package engine;

import java.io.File;
import java.io.InputStream;
import java.util.*;

import TargetGraph.*;

import managers.UserManager;
import task.*;
import types.*;
import utils.FileHandler;

public class Engine implements IEngine {
    FileHandler fileHandler;
    private TargetGraph targetGraph;
    private TaskRunner taskRunner;
    private final TaskManager tasksManager;
    private final UserManager userManager;
    private final Map<String, TargetGraph> allGraphs;

    public Engine() {
        allGraphs = new HashMap<>();
        taskRunner = new TaskRunner(targetGraph);
        tasksManager = new TaskManager();
        userManager = new UserManager();
        fileHandler = new FileHandler();
        userManager.addAdmin("admin");
        try {
            File file = new File("/Users/liorsht/IdeaProjects/GPUP/ex2-big.xml");
            loadXmlFile(file, userManager.getAdmin("admin"));

            File file2 = new File("/Users/liorsht/IdeaProjects/GPUP/ex3-small.xml");
            loadXmlFile(file2, userManager.getAdmin("admin"));

            addTask(new Simulation("small_task"), "small", userManager.getAdmin("admin"));
            addTask(new Simulation("big_task"), "big", userManager.getAdmin("admin"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean toggleTaskRunning(String TaskName) throws Exception {
        return tasksManager.togglePause(TaskName);
    }

    public TaskManager getTasksManager() {
        return tasksManager;
    }

    public void load(TargetGraph _targetGraph) {
        targetGraph = _targetGraph;
    }

    public TargetGraph TargetGraph(String name) {
        return allGraphs.getOrDefault(name, null);
    }


    public String ifNullThenString(Object obj, String instead) {
        return obj == null ? instead : obj.toString();
    }

    public LinkedList<String> findCircuit(String targetName) {
        return targetGraph.findCircuit(targetName);
    }


    public String getResultStatistics() {
        return targetGraph.getStatsInfoString(targetGraph.getResultStatistics());
    }

    public boolean didTaskAlreadyRan() {
        return targetGraph.taskAlreadyRan();
    }

    public void runTask(Task task) {
        taskRunner.initTaskRunner(task);
        taskRunner.run();
    }

    public void runTaskIncrementally() {
        taskRunner.initIncrementalRun();
        taskRunner.run();
    }

    public Map<String, Target> getAllTargets() {
        return targetGraph.getVerticesMap();
    }

    public Map<String, Set<Target>> getAdjacentMap() {
        return targetGraph.getAdjacentMap();
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


    public TargetGraph getTargetGraph() {
        return targetGraph;
    }

    public String getGraphInfo() {
        return targetGraph.getInfo();
    }

    public String check() {
        return "check";
    }

    @Override
    public synchronized Map<String, List<Target>> getTargetsForWorker(String userName, String[] taskNames, int threadsAmount) throws Exception {
        Worker user = userManager.getWorker(userName);
        if (user == null)
            throw new Exception("Not worker");


        return tasksManager.getTargetsForWorker(user, taskNames, threadsAmount);
    }

//    public synchronized void onFinishTaskOnTarget(String userName, String taskName) {
//        tasksManager.onFinishTaskOnTarget(userName, taskName,target);
//    }

    @Override
    public IUser getUser(String name) {
        return null;
    }

    public Set<IUser> getAllUsers() {
        return new HashSet<>(userManager.getUsers().values());
    }

    public Collection<TargetGraph> getAllGraphs() {
        return allGraphs.values();
    }

    @Override
    public Map<String, TargetGraph> getGraphManager() {
        return allGraphs;
    }

    @Override
    public UserManager getUserManager() {
        return userManager;
    }

    @Override
    public void loadXmlFile(InputStream path, Admin createdBy) throws Exception {
        targetGraph = fileHandler.loadGPUPXMLFile(path);
        targetGraph.setCreatedBy(createdBy);
        allGraphs.put(targetGraph.getGraphsName(), targetGraph);
    }

    public void loadXmlFile(File path, Admin createdBy) throws Exception {
        targetGraph = fileHandler.loadGPUPXMLFile(path);
        targetGraph.setCreatedBy(createdBy);
        allGraphs.put(targetGraph.getGraphsName(), targetGraph);
        System.out.println(allGraphs.get(targetGraph.getGraphsName()).getGraphsName());
    }

    @Override
    public void addTask(Task task, String graphName, Admin createdBy, Set<Target> targets) throws Exception {
        TargetGraph targetGraph = allGraphs.getOrDefault(graphName, null);
        if (targetGraph == null)
            throw new Exception("graph wasn't found");

        task.setCreditPerTarget(targetGraph.getPrices().get(TaskType.valueOf(task.getClassName())));
        targetGraph.createNewGraphFromTargetList(targets);
        tasksManager.addTask(task, targetGraph, createdBy);
    }

    public void addTask(Task task, String graphName, Admin createdBy) throws Exception {
        TargetGraph targetGraph = allGraphs.getOrDefault(graphName, null);

        if (targetGraph == null)
            throw new Exception("graph wasn't found");

        task.setCreditPerTarget(targetGraph.getPrices().get(TaskType.valueOf(task.getClassName())));
        tasksManager.addTask(task, targetGraph, createdBy);
    }

    @Override
    public TaskManager getTaskManager() {
        return tasksManager;
    }

}



