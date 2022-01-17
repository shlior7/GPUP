

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Engine {
    private static TargetGraph targetGraph;
    private TaskRunner taskRunner;

    public void toggleTaskRunning() {
        if (taskRunner != null)
            taskRunner.togglePause();
    }

    public static void load(TargetGraph _targetGraph) {
        targetGraph = _targetGraph;
//        threadExecutor = _targetGraph.threadExecutor;
    }

    public static TargetGraph getTargetGraph() {
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

    public static LinkedList<String> findCircuit(String targetName) {
        return targetGraph.findCircuit(targetName);
    }

    public static LinkedList<List<String>> findPaths(String targetName1, String targetName2, boolean dependsOn) {
        return targetGraph.findAllPaths(dependsOn ? targetName1 : targetName2, dependsOn ? targetName2 : targetName1);
    }

    public static String getPostTaskRunInfo() {
        return targetGraph.getPostTaskRunInfo();
    }

    public static Map<String, List<String>> getStatusesStatistics() {
        return targetGraph.getStatusesStatistics();
    }

    public static boolean taskAlreadyRan() {
        return targetGraph.taskAlreadyRan();
    }


    public static boolean NoSuchTarget(String targetName) {
        return targetGraph.NoSuchTarget(targetName);
    }

    public static void setAllFrozensToSkipped() {
        targetGraph.setFrozensToSkipped();
    }

    public void runTask(Task task, int maxParallel) {
        taskRunner = new TaskRunner(targetGraph, task, maxParallel);
        taskRunner.run();
    }
}



