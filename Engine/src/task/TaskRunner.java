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

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class TaskRunner {
    public final AtomicBoolean pause = new AtomicBoolean(false);
    private boolean running = false;
    private final SimpleStringProperty taskOutput;
    private final List<String> taskOutputTotal;
    private final SimpleDoubleProperty progress;
    private final TaskData taskData;
    private Queue<Target> queue;
    private TriConsumerE<String, String, String> logData;


    public TaskRunner(TargetGraph targetGraph, Task task, Admin createdBy, TriConsumerE<String, String, String> log) {
        this.logData = log;
        this.taskOutput = new SimpleStringProperty("");
        this.progress = new SimpleDoubleProperty(0);
        this.taskData = new TaskData(task, targetGraph, createdBy, new AtomicInteger(0));
        this.taskOutputTotal = new LinkedList<>();
    }

    public Task getTask() {
        return taskData.getTask();
    }

    public void initScratchRun() {
        this.progress.set(0);
        this.taskData.setTargetsDone(0);
        pause.set(false);
        running = true;
        setTaskOutput("");
    }

    public void initIncrementalRun() {
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

    public synchronized List<Target> getTargetsForWorker(Worker worker, int amount) {
        if (pause.get() || !running)
            return new ArrayList<>();

        List<Target> targetsToWait = new ArrayList<>();
        List<Target> targetsToSend = new ArrayList<>();

        for (int i = 0; i < amount && !queue.isEmpty(); i++) {
            Target target = queue.poll();
            switch (target.getStatus()) {
                case FROZEN:
                case WAITING:
                    if (!taskData.getTargetGraph().didAllChildrenFinish(target.name)) {
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
            target.setStatus(Status.IN_PROCESS);
        }
        queue.addAll(targetsToWait);
        synchronized (this) {
            setTaskOutput("sending the targets " + targetsToSend + "to the worker " + worker.getName());
        }
        taskData.setWorkersTargets(worker, targetsToSend);
        taskData.getWorkerListMap().put(worker, targetsToSend);
        return targetsToSend;
    }

    public synchronized void OnFinish(String targetName, Result result) throws Exception {
        Target target = taskData.getTargetGraph().getTarget(targetName);
        if (target == null)
            throw new Exception("No such target");

        taskData.getTargetsDoneInteger().incrementAndGet();
        target.setStatus(Status.FINISHED);
        target.setResult(result);
        String name = target.name;
        updateProgress();

        if (target.getResult() == Result.Failure) {
            taskData.getTargetGraph().setParentsStatuses(name, Status.SKIPPED, taskData.getTargetsDoneInteger());
            taskData.getTargetGraph().whoAreAllYourDaddies(name).forEach(t -> {
                try {
                    logData.accept(taskData.getTask().getTaskName(), t.getName(), t.getName() + " was set to skipped");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            updateProgress();
        }

        if (!taskData.getTargetGraph().whoAreYourDirectDaddies(target.name).isEmpty()) {
            logData.accept(taskData.getTask().getTaskName(), target.name, "adding " + taskData.getTargetGraph().whoAreYourDirectDaddies(target.name) + " to waiting queue");
            queue.addAll(taskData.getTargetGraph().whoAreYourDirectDaddies(target.name));
        }

        if (taskData.getTargetsDone() == taskData.getTargetGraph().size()) {
            taskData.setStatus(TaskStatus.FINISHED);
            running = false;
            taskData.setWorkerListMap(new HashMap<>());
        }

    }

    public void resume() {
        pause.set(false);
        taskData.setStatus(TaskStatus.ACTIVE);
    }

    public void pause() {
        pause.set(true);
        taskData.setStatus(TaskStatus.PAUSED);
    }

    public boolean togglePause() {
        if (pause.get()) {
            resume();
        } else {
            pause();
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
        progress.set(taskData.getTargetsDoneInteger().doubleValue() / (double) taskData.getTargetGraph().size());
    }

    public ReadOnlyDoubleProperty getProgress() {
        return progress;
    }

    public void reset() {
        queue.clear();
        taskData.setTargetsDone(taskData.getTargetGraph().size());
    }

    public TargetGraph getGraph() {
        return taskData.getTargetGraph();
    }

    public TaskData getTaskData() {
        return taskData;
    }

    public int getTargetWorkingOn(Worker worker) {
        return taskData.getWorkerListMap().getOrDefault(worker, new ArrayList<>()).size();
    }

    public void signWorkerToTask(Worker worker) {
        taskData.getWorkerListMap().putIfAbsent(worker, new ArrayList<>());
    }

    public void unSignWorkerToTask(Worker worker) {
        try {
            taskData.getWorkerListMap().remove(worker);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isWorkerRegisteredToThisTask(Worker worker) {
        return taskData.getWorkerListMap().containsKey(worker);
    }
}
