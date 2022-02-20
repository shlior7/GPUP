package engine;

import java.io.InputStream;
import java.util.*;

import TargetGraph.*;

import managers.UserManager;
import task.*;
import types.Admin;
import types.IUser;
import types.Task;
import types.Worker;
import utils.FileHandler;

public class Engine implements IEngine {
    private FileHandler fileHandler;
    private TargetGraph targetGraph;
    private TaskRunner taskRunner;
    private final TasksManager tasksManager;
    private final UserManager userManager;
    private final Map<String, TargetGraph> allGraphs;


    public Engine() {
        allGraphs = new HashMap<>();
        fileHandler = new FileHandler();
        taskRunner = new TaskRunner(targetGraph);
        tasksManager = new TasksManager();
        userManager = new UserManager();
    }

    public boolean toggleTaskRunning() {
        if (taskRunner != null)
            return taskRunner.togglePause();
        return false;
    }

    public TasksManager getTasksManager() {
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

    public Map<String, List<String>> getStatusesStatistics() {
        return targetGraph.getStatusesStatistics();
    }

    public String getResultStatistics() {
        return targetGraph.getStatsInfoString(targetGraph.getResultStatistics());
    }

    public boolean didTaskAlreadyRan() {
        return targetGraph.taskAlreadyRan();
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

    public synchronized void signWorkerToTask(String userName, String taskName) throws Exception {
        Worker user = userManager.getWorker(userName);
        if (user == null)
            throw new Exception("Not worker");
        tasksManager.getTask(taskName).setWorkerOnIt(user);
    }

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
        System.out.println(allGraphs.get(targetGraph.getGraphsName()).getGraphsName());
    }


}



