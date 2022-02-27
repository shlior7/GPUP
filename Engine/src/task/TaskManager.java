package task;

import TargetGraph.TargetGraph;
import TargetGraph.Target;
import TargetGraph.Result;
import types.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskManager {
    private final Map<String, TaskRunner> tasksRunners;
    private final Map<Worker, Set<String>> workersTasks;

    public TaskManager() {
        tasksRunners = new HashMap<>();
        workersTasks = new HashMap<>();
    }

    public synchronized void addTask(Task task, TargetGraph graph, Admin createdBy, boolean fromScratch, TriConsumerE<String, String, String> log) {
        TaskRunner taskRunner = new TaskRunner(graph, task, createdBy, log);
        tasksRunners.putIfAbsent(task.getTaskName(), taskRunner);
        if (fromScratch)
            runTaskFromScratch(taskRunner);
        else
            runTaskIncremental(taskRunner);
    }

    public void runTaskFromScratch(TaskRunner task) {
        try {
            task.initScratchRun();
            task.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runTaskIncremental(TaskRunner task) {
        try {
            task.initIncrementalRun();
            task.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Collection<TaskRunner> getAllTasks() {
        return tasksRunners.values();
    }

    public void updateTasks(Map<String, Target[]> targetsToTaskName) {
        targetsToTaskName.forEach(this::updateTask);
    }

    public void updateTask(String taskName, Target[] targets) {
        TaskRunner taskRunner = getTask(taskName);
        taskRunner.getGraph().updateAllTarget(targets);
//        taskRunner.addTaskLog(taskOutputs);
    }

    public TaskRunner getTask(String taskName) {
        return tasksRunners.getOrDefault(taskName, null);
    }

    public synchronized Map<String, List<Target>> getTargetsForWorker(Worker worker, String[] taskNames, int amount) {
        Map<String, List<Target>> targetsToSend = new HashMap<>();
        LinkedList<TaskRunner> tasksOfWorker = new LinkedList<>();

        for (String taskName : taskNames) {
            tasksOfWorker.add(tasksRunners.get(taskName));
        }

        if (tasksOfWorker.stream().noneMatch(TaskRunner::isRunning))
            return new HashMap<>();


        AtomicInteger finalAmount = new AtomicInteger(amount);

        double div = finalAmount.doubleValue() / (double) tasksOfWorker.size();
        System.out.println("div " + div);
        System.out.println("tasks left " + tasksOfWorker.size());
        while (!tasksOfWorker.isEmpty() && div != 0) {
            TaskRunner taskRunner = tasksOfWorker.pop();
            int tasksToAsk = div < 1 ? 1 : (int) div;
            targetsToSend.putIfAbsent(taskRunner.getTask().getTaskName(), new ArrayList<>());

            List<Target> tasksToAdd = taskRunner.getTargetsForWorker(worker, tasksToAsk);
            System.out.println("tasksToAdd " + tasksToAdd);
            if (!tasksToAdd.isEmpty()) {
                targetsToSend.get(taskRunner.getTask().getTaskName()).addAll(tasksToAdd);
                finalAmount.set(finalAmount.get() - tasksToAdd.size());
                div = finalAmount.doubleValue() / (double) tasksOfWorker.size();
            }
        }
        System.out.println("2 targetsToSend = " + targetsToSend);

        System.out.println("worker = " + worker + ", amount = " + amount + " targets " + targetsToSend);
        return targetsToSend;
    }

    public synchronized int onFinishTaskOnTarget(String taskName, String targetName, Result result) throws Exception {
        TaskRunner taskRunner = getTask(taskName);
        if (taskRunner == null)
            return 500;

        taskRunner.OnFinish(targetName, result);
        return 200;
    }

    public Task signUserToTask(Worker worker, String taskName, boolean signTo) {
        TaskRunner taskRunner = getTask(taskName);
        if (taskRunner == null)
            return null;
        workersTasks.putIfAbsent(worker, new HashSet<>());

        if (signTo) {
            taskRunner.signWorkerToTask(worker);
            workersTasks.get(worker).add(taskName);
        } else {
            taskRunner.unSignWorkerToTask(worker);
            workersTasks.get(worker).remove(taskName);
        }

        return taskRunner.getTask();
    }

    public void stopTask(String taskName) throws Exception {
        TaskRunner taskRunner = getTask(taskName);
        if (taskRunner == null)
            throw new Exception("no such task");
        taskRunner.stop();
    }

    public void resumeTask(String taskName) throws Exception {
        TaskRunner taskRunner = getTask(taskName);
        if (taskRunner == null)
            throw new Exception("no such task");
        taskRunner.resume();
    }

    public void pauseTask(String taskName) throws Exception {
        TaskRunner taskRunner = getTask(taskName);
        if (taskRunner == null)
            throw new Exception("no such task");
        taskRunner.pause();
    }

    public boolean togglePause(String taskName) throws Exception {
        TaskRunner taskRunner = getTask(taskName);
        if (taskRunner == null)
            throw new Exception("no such task");
        return taskRunner.togglePause();
    }

    public boolean doesTaskExists(String taskName) {
        return tasksRunners.containsKey(taskName);
    }
}
