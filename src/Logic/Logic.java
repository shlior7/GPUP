package Logic;

import UI.FileHandler;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Logic {
    private static TargetGraph targetGraph;
    private static final FileHandler fileHandler = new FileHandler();


    public static void load(String xmlPath) throws Exception {
            targetGraph = fileHandler.loadGPUPXMLFile(xmlPath);
    }

    public static boolean validateGraph() {
        return targetGraph != null;
    }

    public static String graphInfo() {
        if(!validateGraph())
            return "no target graph found";
        return targetGraph.toString();
    }

    public static String targetInfo(String name) {
        if(!validateGraph())
            return "no target graph found";
        return targetGraph.getTargetInfo(name);
    }

    public static void runTaskOnTargets(Task task, boolean startFromLastPoint) throws IOException, InterruptedException {
        FileHandler.logLibPath = createLogLibrary(task.getName());
        if (startFromLastPoint) {
             targetGraph.runTaskFromLastTime(task);
        } else {
             targetGraph.runTaskFromScratch(task);
        }
    }
    public static String ifNullThenString(Object obj,String instead){
        return obj == null ? instead: obj.toString();
    }

    public static void save(String xmlPath) throws TransformerException {
        fileHandler.saveToXML(targetGraph, xmlPath);
    }

    public static LinkedList<String> findCircuit(String targetName) {
        return targetGraph.findCircuit(targetName);
    }

    public static LinkedList<List<String>> findPath(String targetName1, String targetName2, boolean dependsOn) {
        return targetGraph.printAllPaths(dependsOn ? targetName1 : targetName2, dependsOn ? targetName2 : targetName1);
    }

    public static String getPostTaskRunInfo() {
        return targetGraph.getPostTaskRunInfo();
    }

    public static Map<String,List<String>> getStatusesStatistics() {
        return targetGraph.getStatusesStatistics();
    }

    public static boolean taskAlreadyRan() {
        return targetGraph.taskAlreadyRan();
    }

    public static String createLogLibrary(String taskName) {
        String currentTime = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").format(LocalDateTime.now());
        String path = taskName + " - " + currentTime;
        new File(path).mkdir();
        return path;
    }

    public static boolean NoSuchTarget(String targetName) {
        return targetGraph.NoSuchTarget(targetName);
    }
}

