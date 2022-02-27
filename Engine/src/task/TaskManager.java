package task;

import TargetGraph.TargetGraph;
import TargetGraph.Target;
import TargetGraph.Result;
import types.Admin;
import types.Task;
import types.Worker;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TaskManager {
    private final Map<String, TaskRunner> tasksRunners;
    private final Map<Worker, Set<String>> workersTasks;

    public TaskManager() {
        tasksRunners = new HashMap<>();
        workersTasks = new HashMap<>();
    }

    public synchronized void addTask(Task task, TargetGraph graph, Admin createdBy) {
        TaskRunner taskRunner = new TaskRunner(graph, task, createdBy);
        tasksRunners.putIfAbsent(task.getTaskName(), taskRunner);
        runTask(taskRunner);
    }

    public void runTask(TaskRunner task) {
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

    public synchronized void startTask() {
    }


    public synchronized void stopTask() {
    }

    public synchronized void pauseTask() {
    }

    public synchronized void resumeTask() {
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

    public synchronized List<Task> getTasksForWorker(Worker worker, LinkedList<TaskRunner> tasks, int amount) {
        Map<String, List<Task>> tasksToSend = new HashMap<>();
        Map<String, List<Target>> targetsToSend = new HashMap<>();

        AtomicInteger finalAmount = new AtomicInteger(amount);
        workersTasks.getOrDefault(worker, new HashSet<>()).forEach((task) -> {
            TaskRunner taskRunner = tasksRunners.get(task);
            if (taskRunner.getTargetWorkingOn(worker) == 0 && finalAmount.get() > 0) {
                tasksToSend.put(task, taskRunner.getTasksForWorker(worker, 1));
                finalAmount.getAndDecrement();
            }
        });

        double div = finalAmount.doubleValue() / (double) tasks.size();
        while (!tasks.isEmpty() && div != 0) {
            TaskRunner taskRunner = tasks.pop();
            int tasksToAsk = div < 1 ? 1 : (int) div;
            tasksToSend.putIfAbsent(taskRunner.getTask().getTaskName(), new ArrayList<>());

            List<Task> tasksToAdd = taskRunner.getTasksForWorker(worker, tasksToAsk);
            if (!tasksToAdd.isEmpty()) {
                tasksToSend.get(taskRunner.getTask().getTaskName()).addAll(tasksToAdd);
                amount -= tasksToAdd.size();
                div = (double) amount / (double) tasks.size();
            }
        }
        return null;
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

    public boolean togglePause(String taskName) throws Exception {
        TaskRunner taskRunner = getTask(taskName);
        if (taskRunner == null)
            throw new Exception("no such task");
        return taskRunner.togglePause();
    }
}
