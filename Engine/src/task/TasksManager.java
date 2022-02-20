package task;

import TargetGraph.TargetGraph;
import types.Task;

import java.util.HashMap;

public class TasksManager {
    private final HashMap<Task, TargetGraph> graphs;
    private final HashMap<String, TaskRunner> tasksRunners;

    public TasksManager() {
        tasksRunners = new HashMap<>();
        graphs = new HashMap<>();
    }

    public synchronized void addTask(Task task, TargetGraph graph) {
        graphs.putIfAbsent(task, graph);
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
