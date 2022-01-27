package engine;

import java.util.*;

import TargetGraph.*;
import task.*;

public class Engine {
    private static TargetGraph targetGraph;
    private TaskRunner taskRunner;

    public Engine() {
        taskRunner = new TaskRunner(targetGraph);
    }

    public boolean toggleTaskRunning() {
        if (taskRunner != null)
            return taskRunner.togglePause();
        return false;
    }

    public static void load(TargetGraph _targetGraph) {
        targetGraph = _targetGraph;
    }

    public static TargetGraph TargetGraph() {
        return targetGraph;
    }

    public static boolean validateGraph() {
        return targetGraph != null;
    }

    public static String graphInfo() {
        if (!validateGraph())
            return "no target graph found";
        return targetGraph.toString();
    }

    public static String targetInfo(String name) {
        if (!validateGraph())
            return "no target graph found";
        return targetGraph.getTargetInfo(name);
    }

    public static Queue<Target> InitTaskAndGetRunningQueue(boolean startFromLastPoint) {
        if (startFromLastPoint) {
            return targetGraph.getQueueFromLastTime();
        } else {
            return targetGraph.getQueueFromScratch();
        }
    }

    public static String ifNullThenString(Object obj, String instead) {
        return obj == null ? instead : obj.toString();
    }

    public LinkedList<String> findCircuit(String targetName) {
        return targetGraph.findCircuit(targetName);
    }

    public static Map<String, List<String>> getStatusesStatistics() {
        return targetGraph.getStatusesStatistics();
    }

    public String getResultStatistics() {
        return targetGraph.getStatsInfoString(targetGraph.getResultStatistics());
    }

    public static boolean taskAlreadyRan() {
        return targetGraph.taskAlreadyRan();
    }

    public boolean didTaskAlreadyRan() {
        return targetGraph.taskAlreadyRan();
    }

    public static void setAllFrozensToSkipped() {
        targetGraph.setFrozensToSkipped();
    }

    public void runTask(Task task, int maxParallel) {
        taskRunner.initTaskRunner(task, maxParallel);
        taskRunner.run();
    }

    public void runTaskIncrementally() {
        taskRunner.initIncrementalRun();
        taskRunner.run();
    }

    public Map<String, Target> getAllTargets() {
        return targetGraph.getAllElementMap();
    }

    public Map<String, Set<Target>> getAdjacentMap() {
        return targetGraph.getAdjacentNameMap();
    }


    public boolean isTaskRunning() {
        if (taskRunner == null)
            return false;
        return taskRunner.isRunning();
    }

    public TaskRunner getTaskRunner() {
        return taskRunner;
    }

    public void reset() {
        targetGraph.reset();
        taskRunner.reset();
    }

    public boolean createNewGraphFromTargetList(Set<Target> targetToRunOn) {
        return targetGraph.createNewGraphFromTargetList(targetToRunOn);
    }

    public LinkedList<List<String>> findAllPaths(String source, String destination) {
        return targetGraph.findAllPaths(source, destination);
    }


    public int getMaxThreads() {
        return targetGraph.getMaxThreads();
    }

    public TargetGraph getTargetGraph() {
        return targetGraph;
    }

    public String getGraphInfo() {
        return targetGraph.getInfo();
    }
}



