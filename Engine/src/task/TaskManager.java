package task;

import TargetGraph.TargetGraph;
import types.Admin;
import types.Task;

import java.util.Collection;
import java.util.HashMap;

public class TaskManager {
    private final HashMap<String, TaskRunner> tasksRunners;

    public TaskManager() {
        tasksRunners = new HashMap<>();
    }

    public synchronized void addTask(Task task, TargetGraph graph, Admin createdBy) {
        TaskRunner taskRunner = new TaskRunner(graph, task, createdBy);
        tasksRunners.putIfAbsent(task.getTaskName(), taskRunner);

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

    public TaskRunner getTask(String taskName) {
        return tasksRunners.getOrDefault(taskName, null);
    }
}
