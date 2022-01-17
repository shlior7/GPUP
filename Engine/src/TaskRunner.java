import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskRunner implements Runnable {
    private final TargetGraph targetGraph;
    private static ExecutorService threadExecutor;
    private Queue<Target> queue;
    private final AtomicInteger targetsDone = new AtomicInteger(0);
    public final AtomicBoolean pause = new AtomicBoolean(false);
    private Task task;
    int prev = -1;

    public TaskRunner(TargetGraph targetGraph, Task task, int maxParallelism) {
        this.targetGraph = targetGraph;
        threadExecutor = Executors.newFixedThreadPool(maxParallelism);
        this.task = task;
    }

    @Override
    public void run() {
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
                runTaskOnTarget(target, new Simulation((Simulation) task));
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

        }
        System.out.println("Im done!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        targetGraph.printStatsInfo(targetGraph.getResultStatistics());
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
        Task newTask = new Simulation((Simulation) task);
        synchronized (this) {
            System.out.println("running task on target: " + target.name);
        }

        newTask.setTarget(target);
        newTask.setFuncOnFinished(this::OnFinish);
        synchronized (this) {
            System.out.println("Adding task " + newTask.getName() + " to thread pool.");
        }
        threadExecutor.execute(newTask);
        ssc.setBusy(target.name, true);
        synchronized (this) {
            targetsDone.incrementAndGet();
            System.out.println("++++++++++++++++++++++++++++++++");
            System.out.println("target: " + target.name + " started to run and now its : " + targetsDone.get());
            System.out.println("++++++++++++++++++++++++++++++++");
        }
    }


    public synchronized void OnFinish(Target target, Task task) {
        target.setStatus(Status.FINISHED);
        String name = target.name;
        System.out.println("finished task " + name);
        targetGraph.getSerialSets().setBusy(name, false);

        if (target.getResult() == Result.Failure) {
            targetGraph.setParentsStatuses(name, Status.SKIPPED, targetsDone);
        }

        queue.addAll(targetGraph.whoAreYourDaddies(target.name));
        if (prev != targetsDone.get()) {
            System.out.println("done : " + targetsDone.get());
            System.out.println("status = " + target.getStatus() + ",result = " + target.getResult());
            prev = targetsDone.get();
        }
    }

    public void togglePause() {
        System.out.println("XXXXXXXXXXXXXXXXXXXXX");
        pause.set(!pause.get());
        if (!pause.get()) {
            synchronized (targetGraph) {
                targetGraph.notifyAll();
                System.out.println("notify!!!");
            }
        }
        System.out.println("paused: " + pause.get());
    }
}
