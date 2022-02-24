package task;

import TargetGraph.Result;
import TargetGraph.Status;
import TargetGraph.Target;
import TargetGraph.TargetGraph;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.SneakyThrows;
import types.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class TaskRunner implements Runnable {
    private Map<Worker, List<Target>> workerListMap;
    private final TargetGraph targetGraph;
    private ExecutorService threadExecutor;
    private Queue<Target> queue;
    private final AtomicInteger targetsDone = new AtomicInteger(0);
    public final AtomicBoolean pause = new AtomicBoolean(false);
    private Task task;
    private boolean running = false;
    private int numThread;
    private final SimpleStringProperty taskOutput;
    private final List<String> taskOutputTotal;
    private final SimpleDoubleProperty progress;
    private TaskData taskData;

    public TaskRunner(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
        this.taskOutput = new SimpleStringProperty("");
        this.progress = new SimpleDoubleProperty(0);
        this.numThread = 1;
        this.taskOutputTotal = new LinkedList<>();
    }

    public TaskRunner(TargetGraph targetGraph, Task task, Admin createdBy) {
        this.workerListMap = new HashMap<>();
        this.task = task;
        this.targetGraph = targetGraph;
        this.taskOutput = new SimpleStringProperty("");
        this.progress = new SimpleDoubleProperty(0);
        this.numThread = 1;
        this.taskData = new TaskData(task, targetGraph, createdBy);
        this.taskOutputTotal = new LinkedList<>();
    }

    public Task getTask() {
        return task;
    }

    public void initTaskRunner(Task task) {
        initTask(task);
        initIncrementalRun();
    }

    public void initTask(Task task) {
        this.task = task;
    }

    public void initIncrementalRun() {
        this.threadExecutor = Executors.newFixedThreadPool(numThread);
        this.progress.set(0);
        this.targetsDone.set(0);
        pause.set(false);
        running = true;
        setTaskOutput("");
    }

    public void start() {
        queue = taskData.getTargetGraph().initQueue();
        taskData.setStatus(TaskStatus.ACTIVE);
    }

    public void stop() {
        running = false;
        taskData.setStatus(TaskStatus.STOPPED);
        pause.set(false);
    }

    @SneakyThrows
    @Override
    public void run() {
        queue = targetGraph.initQueue();
        while (targetsDone.get() < targetGraph.size() || !queue.isEmpty()) {
            if (pause.get()) {
                try {
                    synchronized (targetGraph) {
                        setTaskOutput("Paused!");
                        targetGraph.wait();
                        setTaskOutput("Resumed!");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!queue.isEmpty()) {
                Target target = queue.poll();
                runTaskOnTarget(target);
            }
        }
        threadExecutor.shutdown();

        while (!threadExecutor.isTerminated()) {
            Thread.sleep(1000);
        }

        setTaskOutput("Done!");
        setTaskOutput(targetGraph.getStatsInfoString(targetGraph.getResultStatistics()));
        running = false;
        pause.set(false);
    }


    public synchronized void runTaskOnTarget(Target target) {
        switch (target.getStatus()) {
            case FROZEN:
            case WAITING:
                if (!targetGraph.didAllChildrenFinish(target.name)) {
                    if (target.getStatus() == Status.FROZEN)
                        target.setWaitingTime(Instant.now());
                    target.setStatus(Status.WAITING);
                    queue.add(target);
                    return;
                }
                break;
            default:
                return;
        }

        synchronized (this) {
            setTaskOutput("running task on target: " + target.name);
        }
        threadExecutor.execute(initTask(target));
        targetsDone.incrementAndGet();
    }

    public synchronized List<Task> getTasksForWorker(Worker worker, int amount) {
        if (pause.get() || !running)
            return new ArrayList<>();


        List<Target> targetsToWait = new ArrayList<>();
        List<Target> targetsToSend = new ArrayList<>();
        List<Task> tasksToSend = new ArrayList<>();
        for (int i = 0; i < amount && !queue.isEmpty(); i++) {
            Target target = queue.poll();
            switch (target.getStatus()) {
                case FROZEN:
                case WAITING:
                    if (!targetGraph.didAllChildrenFinish(target.name)) {
                        if (target.getStatus() == Status.FROZEN)
                            target.setWaitingTime(Instant.now());
                        target.setStatus(Status.WAITING);
                        targetsToWait.add(target);
                        continue;
                    }
                    break;
                default:
                    continue;
            }
            targetsToSend.add(target);
            tasksToSend.add(initTask(target));
        }
        queue.addAll(targetsToWait);
        synchronized (this) {
            setTaskOutput("sending the targets " + targetsToSend + "to the worker " + worker.getName());
        }
        taskData.setWorkersTargets(worker, targetsToSend);
        workerListMap.put(worker, targetsToSend);
        return tasksToSend;
    }

    public Task initTask(Target target) {
        Task newTask = task.copy();
        newTask.setTarget(target);
        newTask.setOutputText(this::setTaskOutput);
        return newTask;
    }

    public void updateOnTaskStatus(Task task) {

    }

    public void getUpdateOnTaskStatus(Task task) {

    }

    public synchronized void OnFinish(String targetName) throws Exception {
        Target target = taskData.getTargetGraph().getTarget(targetName);
        if (target == null)
            throw new Exception("No such target");

        targetsDone.incrementAndGet();
        target.setStatus(Status.FINISHED);
        String name = target.name;
        updateProgress();
        setTaskOutput("finished task " + name + " with the result " + target.getResult() + " time it took to process " + target.getProcessTime().toMillis());

        if (target.getResult() == Result.Failure) {
            targetGraph.setParentsStatuses(name, Status.SKIPPED, targetsDone);
            targetGraph.whoAreAllYourDaddies(name).forEach(t -> setTaskOutput(t.getName() + " was set to skipped"));
            updateProgress();
        }

        if (!targetGraph.whoAreYourDirectDaddies(target.name).isEmpty()) {
            setTaskOutput("adding " + targetGraph.whoAreYourDirectDaddies(target.name) + " to waiting queue");
            queue.addAll(targetGraph.whoAreYourDirectDaddies(target.name));
        }

        if (targetsDone.get() == targetGraph.size()) {
            taskData.setStatus(TaskStatus.FINISHED);
        }

    }

    public void resume() {
        synchronized (targetGraph) {
            targetGraph.notifyAll();
        }
    }

    public boolean togglePause() {
        pause.set(!pause.get());
        if (pause.get()) {
            taskData.setStatus(TaskStatus.PAUSED);
        } else {
            taskData.setStatus(TaskStatus.ACTIVE);
        }
        return pause.get();
    }

    public synchronized void addTaskLog(String[] text) {
        taskOutputTotal.addAll(Arrays.asList(text));
    }

    public List<String> getAllTaskOutput() {
        return taskOutputTotal;
    }

    public synchronized void setTaskOutput(String text) {
        taskOutput.set(text);
    }

    public SimpleStringProperty getTaskOutput() {
        return taskOutput;
    }

    public boolean isRunning() {
        return running;
    }

    public synchronized void updateProgress() {
        progress.set(targetsDone.doubleValue() / (double) targetGraph.size());
    }

    public ReadOnlyDoubleProperty getProgress() {
        return progress;
    }

    public void reset() {
        queue.clear();
        targetsDone.set(targetGraph.size());
    }

    public TargetGraph getGraph() {
        return targetGraph;
    }

    public TaskData getTaskData() {
        return taskData;
    }

    public int getTargetWorkingOn(Worker worker) {
        return taskData.getWorkerListMap().getOrDefault(worker, new ArrayList<>()).size();
    }
}
