package task;

import TargetGraph.Result;
import TargetGraph.Status;
import TargetGraph.Target;
import TargetGraph.TargetGraph;
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

import TargetGraph.SerialSetController;

public class TaskRunner implements Runnable {
    private final TargetGraph targetGraph;
    private ExecutorService threadExecutor;
    private Queue<Target> queue;
    private final AtomicInteger targetsDone = new AtomicInteger(0);
    public final AtomicBoolean pause = new AtomicBoolean(false);
    private Task task;
    private boolean finished = false;
    private boolean running = false;
    private boolean runFromScratch;
    private final SimpleStringProperty taskOutput;
    private final SimpleDoubleProperty progress;


    public TaskRunner(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
        this.taskOutput = new SimpleStringProperty("");
        this.progress = new SimpleDoubleProperty(0);
    }

    public void initTaskRunner(Task task, int maxParallelism, boolean runFromScratch) {
        this.finished = false;
        this.threadExecutor = Executors.newFixedThreadPool(maxParallelism);
        this.task = task;
        this.runFromScratch = runFromScratch;
        this.progress.set(0);
        this.targetsDone.set(0);
    }

    @SneakyThrows
    @Override
    public void run() {
        running = true;
        pause.set(false);
        if (runFromScratch) {
            resetCurrentGraph();
            queue = targetGraph.initQueue();
        } else {
            queue = targetGraph.getQueueFromLastTime();
            targetsDone.addAndGet((int) targetGraph.getCurrentTargets().stream().filter(t -> t.getResult() == Result.Success || t.getResult() == Result.Warning).count());
        }

        while (targetsDone.get() < targetGraph.size()) {
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
                runTaskOnTarget(target, task.copy());
            }
        }
        threadExecutor.shutdown();

        while (!threadExecutor.isTerminated()) {
            Thread.sleep(1000);
        }

        setTaskOutput("Done!");
        setTaskOutput(targetGraph.getStatsInfoString(targetGraph.getResultStatistics()));
        running = false;
        finished = true;
    }

    public synchronized void runTaskOnTarget(Target target, Task task) {
        SerialSetController ssc = targetGraph.getSerialSets();
        switch (target.getStatus()) {
            case FROZEN:
            case WAITING:
                if (!targetGraph.didAllChildrenFinish(target.name) || ssc.isBusy(target.name)) {
                    if (target.getStatus() == Status.FROZEN)
                        target.setWaitingTime(Instant.now());
                    target.setStatus(Status.WAITING);
                    queue.add(target);
                    return;
                }
                break;
            case FINISHED:
                if (target.getResult() == Result.Success || target.getResult() == Result.Warning) {
                    queue.addAll(targetGraph.whoAreYourDirectDaddies(target.name));
                }
            default:
                return;
        }

        synchronized (this) {
            setTaskOutput("running task on target: " + target.name);
        }
        threadExecutor.execute(initTask(target));
        targetsDone.incrementAndGet();
        ssc.setBusy(target.name, true);
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

        targetGraph.getSerialSets().setBusy(name, false);

        if (target.getResult() == Result.Failure) {
            targetGraph.setParentsStatuses(name, Status.SKIPPED, targetsDone);
            targetGraph.whoAreAllYourDaddies(name).forEach(t -> setTaskOutput(t.getName() + " was set to skipped"));
            updateProgress();
        }

        queue.addAll(targetGraph.whoAreYourDirectDaddies(target.name));
    }


    public boolean togglePause() {
        pause.set(!pause.get());
        if (!pause.get()) {
            synchronized (targetGraph) {
                targetGraph.notifyAll();
            }
        }
        return pause.get();
    }

    private void resetCurrentGraph() {
        targetGraph.reset();
    }

    public synchronized void setTaskOutput(String text) {
        taskOutput.set(text);
    }

    public SimpleStringProperty getTaskOutput() {
        return taskOutput;
    }

    public boolean isFinished() {
        return finished;
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
}
