package engine.task;

import TargetGraph.*;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.SneakyThrows;

import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskRunner implements Runnable {
    private final TargetGraph targetGraph;
    private ExecutorService threadExecutor;
    private Queue<Target> queue;
    private final AtomicInteger targetsDone = new AtomicInteger(0);
    public final AtomicBoolean pause = new AtomicBoolean(false);
    private Task task;
    private boolean running = false;
    private int numThread;
    private final SimpleStringProperty taskOutput;
    private final SimpleDoubleProperty progress;


    public TaskRunner(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
        this.taskOutput = new SimpleStringProperty("");
        this.progress = new SimpleDoubleProperty(0);
        this.numThread = 0;
    }

    public void initTaskRunner(Task task, int maxParallelism) {
        initTask(task, maxParallelism);
        numThread = maxParallelism;
        initIncrementalRun();
    }

    public void initTask(Task task, int maxParallelism) {
        this.task = task;
        this.numThread = maxParallelism;
    }

    public void initIncrementalRun() {
        this.threadExecutor = Executors.newFixedThreadPool(numThread);
        this.progress.set(0);
        this.targetsDone.set(0);
        pause.set(false);
        running = true;
        setTaskOutput("");
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
        resume();
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

    public Task initTask(Target target) {
        Task newTask = task.copy();
        newTask.setTarget(target);
        newTask.setFuncOnFinished(this::OnFinish);
        newTask.setOutputText(this::setTaskOutput);
        return newTask;
    }

    public synchronized void OnFinish(Target target) {
        target.setStatus(Status.FINISHED);
        String name = target.name;
        updateProgress();
        setTaskOutput("finished task " + name + " with the result " + target.getResult() + " time it took to process " + target.getProcessTime().toMillis());

        if (!targetGraph.whoAreYourDirectDaddies(target.name).isEmpty()) {
            setTaskOutput("adding " + targetGraph.whoAreYourDirectDaddies(target.name) + " to waiting queue");
            queue.addAll(targetGraph.whoAreYourDirectDaddies(target.name));
        }

        if (target.getResult() == Result.Failure) {
            targetGraph.setParentsStatuses(name, Status.SKIPPED, targetsDone);
            targetGraph.whoAreAllYourDaddies(name).forEach(t -> setTaskOutput(t.getName() + " was set to skipped"));
            updateProgress();
        }
    }

    public void resume() {
        synchronized (targetGraph) {
            targetGraph.notifyAll();
        }
    }

    public boolean togglePause() {
        pause.set(!pause.get());
        if (!pause.get()) {
            resume();
        }
        return pause.get();
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
        resume();
    }
}
