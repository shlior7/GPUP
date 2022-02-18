package task;

import TargetGraph.TargetGraph;

import java.util.HashMap;

public class TasksManager {
    private final HashMap<String, Task> tasksList;
    private final HashMap<Task, TargetGraph> graphs;
    private final HashMap<String, TaskRunner> tasksRunners;

    public TasksManager() {
        tasksList = new HashMap<>();
        tasksRunners = new HashMap<>();
        graphs = new HashMap<>();
    }

    public synchronized void addTask(Task task, TargetGraph graph) {
        tasksList.putIfAbsent(task.getName(), task);
        graphs.putIfAbsent(task, graph);
    }

    public synchronized Task getTask(String taskName) {
        return tasksList.getOrDefault(taskName, null);
    }

    public synchronized void startTask() {
    }


    public synchronized void stopTask() {
    }

    public synchronized void pauseTask() {
    }

    public synchronized void resumeTask() {
    }
}
