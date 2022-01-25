import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import lombok.SneakyThrows;

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
    int prev = -1;
    private boolean finished = false;
    private boolean running = false;
    private boolean runFromScratch;
    private SimpleStringProperty taskOutput;


    public TaskRunner(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
        this.taskOutput = new SimpleStringProperty("");
    }

    public void initTaskRunner(Task task, int maxParallelism, boolean runFromScratch) {
        this.threadExecutor = Executors.newFixedThreadPool(maxParallelism);
        this.task = task;
        this.runFromScratch = runFromScratch;
    }

    @SneakyThrows
    @Override
    public void run() {
        running = true;
        queue = targetGraph.initQueue();

        int prev = -1;
        while (targetsDone.get() != targetGraph.size()) {
            if (pause.get()) {
                try {
                    synchronized (targetGraph) {
                        System.out.println("wait");
                        targetGraph.wait();
                        System.out.println("notified");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!queue.isEmpty()) {
                Target target = queue.poll();
                runTaskOnTarget(target, task.copy());
            }
            if (prev != targetsDone.get()) {
                System.out.println("number of targets Done: " + targetsDone.get());
                System.out.println("number of targets: " + targetGraph.size());
                prev = targetsDone.get();
                targetGraph.printStatsInfo(targetGraph.getStatusesStatistics());
            }
        }
        threadExecutor.shutdown();

        while (!threadExecutor.isTerminated()) {
            System.out.println("not Done!!!!!!");
            Thread.sleep(1000);
        }
        System.out.println("Im done!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        targetGraph.printStatsInfo(targetGraph.getResultStatistics());
        targetGraph.printStatsInfo(targetGraph.getStatusesStatistics());
        running = false;
        finished = true;
    }

    public synchronized void runTaskOnTarget(Target target, Task task) {
        SerialSetController ssc = targetGraph.getSerialSets();
        switch (target.getStatus()) {
            case FROZEN:
            case WAITING:
                if (!targetGraph.didAllChildrenFinish(target.name) || ssc.isBusy(target.name)) {
                    target.setStatus(Status.WAITING);
                    queue.add(target);
                    return;
                }
                break;

            default:
                return;
        }

        synchronized (this) {
            Platform.runLater(() -> {
                setTaskOutput("running task on target: " + target.name);
            });

            System.out.println("running task on target: " + target.name);
        }
        threadExecutor.execute(initTask(target));

        ssc.setBusy(target.name, true);
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
        Platform.runLater(() -> {
            setTaskOutput("finished task " + name + " with the result " + target.getResult());
        });
        System.out.println("finished task " + name + " with the result " + target.getResult());

        targetGraph.getSerialSets().setBusy(name, false);

        if (target.getResult() == Result.Failure) {
            targetGraph.setParentsStatuses(name, Status.SKIPPED, targetsDone);
            targetGraph.whoAreYourAllDaddies(name).forEach(t -> setTaskOutput(t.getName() + " was set to skipped"));
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


}
