package task;

import TargetGraph.TargetGraph;
import TargetGraph.Target;
import types.Admin;
import types.Task;
import types.Worker;

import java.util.*;

public class TaskManager {
    private final HashMap<String, TaskRunner> tasksRunners;

    public TaskManager() {
        tasksRunners = new HashMap<>();
    }

    public synchronized void addTask(Task task, TargetGraph graph, Admin createdBy) {
        TaskRunner taskRunner = new TaskRunner(graph, task, createdBy);
        tasksRunners.putIfAbsent(task.getTaskName(), taskRunner);
        runTask(taskRunner);
    }

    public void runTask(TaskRunner task) {
        Thread.UncaughtExceptionHandler handler = (th, ex) -> System.out.println("Uncaught exception: " + ex);
        Thread work = new Thread(() -> {
            task.initIncrementalRun();
            task.start();
        }, "TaskRunner");
        work.setUncaughtExceptionHandler(handler);
        work.start();
    }

    public Collection<TaskRunner> getAllTasks() {
        return tasksRunners.values();
    }

    public synchronized void startTask() {
    }


    public synchronized void stopTask() {
    }

    public synchronized void pauseTask() {
    }

    public synchronized void resumeTask() {
    }

    public void updateTask(String taskName, Target[] targets, String[] taskOutputs) {
        TaskRunner taskRunner = getTask(taskName);
        taskRunner.getGraph().updateAllTarget(targets);
        taskRunner.addTaskLog(taskOutputs);
    }

    public TaskRunner getTask(String taskName) {
        return tasksRunners.getOrDefault(taskName, null);
    }

    public synchronized List<Task> getTasksForWorker(Worker worker, LinkedList<TaskRunner> tasks, int amount) {
        Map<String, List<Task>> tasksToSend = new HashMap<>();
        double div = (double) amount / (double) tasks.size();
        while (!tasks.isEmpty() && div != 0) {
            TaskRunner taskRunner = tasks.pop();
            int tasksToAsk = div < 1 ? 1 : (int) div;
            tasksToSend.put(taskRunner.getTask().getTaskName(), taskRunner.getTasksForWorker(worker, tasksToAsk));
            amount -= tasksToAsk;
            div = (double) amount / (double) tasks.size();
        }
        return null;
    }

    public synchronized String onFinishTaskOnTarget(String userName, String taskName, String targetName) throws Exception {
        TaskRunner taskRunner = getTask(taskName);
        if (taskRunner == null)
            return "no such task";

        taskRunner.OnFinish(targetName);
        return "OK";
    }

}
