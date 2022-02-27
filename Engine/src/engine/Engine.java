package engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import TargetGraph.*;

import managers.UserManager;
import task.*;
import types.*;
import utils.FileHandler;

public class Engine implements IEngine {
    FileHandler fileHandler;
    private final TaskManager tasksManager;
    private final UserManager userManager;
    private final Map<String, TargetGraph> allGraphs;
    private Map<String, Map<String, String[]>> logs;
    private AtomicBoolean writing = new AtomicBoolean(false);

    public Engine() {
        logs = new HashMap<>();
        allGraphs = new HashMap<>();
        tasksManager = new TaskManager();
        userManager = new UserManager();
        fileHandler = new FileHandler();
        userManager.addAdmin("admin");
        try {
            File file = new File("/Users/liorsht/IdeaProjects/GPUP/ex2-big.xml");
            loadXmlFile(file, userManager.getAdmin("admin"));

//            File file2 = new File("/Users/liorsht/IdeaProjects/GPUP/ex3-small.xml");
//            loadXmlFile(file2, userManager.getAdmin("admin"));

//            addTask(new Simulation("small_task"), "small", userManager.getAdmin("admin"));
            addTask(new Simulation("big_task"), "big", userManager.getAdmin("admin"), true);
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


    public TargetGraph TargetGraph(String name) {
        return allGraphs.getOrDefault(name, null);
    }


    public String ifNullThenString(Object obj, String instead) {
        return obj == null ? instead : obj.toString();
    }


    @Override
    public synchronized Map<String, List<Target>> getTargetsForWorker(String userName, String[] taskNames, int threadsAmount) throws Exception {
        Worker user = userManager.getWorker(userName);
        if (user == null)
            throw new Exception("Not worker");


        return tasksManager.getTargetsForWorker(user, taskNames, threadsAmount);
    }

    public synchronized void postLogs(String taskName, String targetName, String data) throws IOException {
        fileHandler.log(data, taskName, targetName);

//        List<String> logged = Arrays.asList(logs.getOrDefault(taskName, new HashMap<>()).getOrDefault(targetName, new String[0]));
//        logged.add(data);
        String[] logged = addToData(logs.getOrDefault(taskName, new HashMap<>()).getOrDefault(targetName, new String[1]), data);
        logs.getOrDefault(taskName, new HashMap<>()).put(targetName, logged);
    }

    public String[] addToData(String arr[], String x) {
        List<String> arrlist
                = new ArrayList<>(
                Arrays.asList(arr));
        arrlist.add(x);

        arr = arrlist.toArray(arr);
        return arr;
    }

    @Override
    public synchronized List<String> getLogs(String taskName) {
        if (taskName == null || taskName.isEmpty() || !logs.containsKey(taskName))
            return new ArrayList<>();

        List<String> all = new ArrayList<>();
        logs.get(taskName).values().forEach((log) -> {
            all.addAll(Arrays.asList(log));
        });
        return all.stream().map(s -> "\"" + s + "\"").collect(Collectors.toList());
    }

    @Override
    public synchronized void postLogs(Map<String, Map<String, String[]>> logsToPost) {
        logs = logsToPost;

        Thread thread = new Thread(() -> {
            for (String taskName : logsToPost.keySet()) {
                for (String targetName : logsToPost.get(taskName).keySet()) {
                    try {
                        fileHandler.clearLogFile(taskName, targetName);
                        for (String data : logsToPost.get(taskName).get(targetName)) {
                            fileHandler.log(data, taskName, targetName);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
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
        TargetGraph targetGraph = fileHandler.loadGPUPXMLFile(path);
        targetGraph.setCreatedBy(createdBy);
        allGraphs.put(targetGraph.getGraphsName(), targetGraph);
    }

    public void loadXmlFile(File path, Admin createdBy) throws Exception {
        TargetGraph targetGraph = fileHandler.loadGPUPXMLFile(path);
        targetGraph.setCreatedBy(createdBy);
        allGraphs.put(targetGraph.getGraphsName(), targetGraph);
        System.out.println(allGraphs.get(targetGraph.getGraphsName()).getGraphsName());
    }

    @Override
    public void addTask(Task task, String graphName, Admin createdBy, Set<Target> targets, boolean fromScratch) throws Exception {
        TargetGraph targetGraph = allGraphs.getOrDefault(graphName, null);
        if (targetGraph == null)
            throw new Exception("graph wasn't found");

        System.out.println("task = " + task.getTaskName() + ", graphName = " + targetGraph.getStatsInfoString(targetGraph.getStatusesStatisticsString()) + "\n createdBy = " + createdBy.getName() + ", fromScratch = " + fromScratch);
        fileHandler.createLogLibrary(task.getTaskName());
        if (fromScratch)
            targetGraph.getCurrentTargets().forEach(t -> t.init(targetGraph.createTargetInGraphInfo(t)));

        task.setCreditPerTarget(targetGraph.getPrices().get(TaskType.valueOf(task.getClassName())));
        targetGraph.createNewGraphFromTargetList(targets);
        tasksManager.addTask(task, targetGraph, createdBy, fromScratch, this::postLogs);
    }

    public void addTask(Task task, String graphName, Admin createdBy, boolean fromScratch) throws Exception {
        TargetGraph targetGraph = allGraphs.getOrDefault(graphName, null);

        if (targetGraph == null)
            throw new Exception("graph wasn't found");

        task.setCreditPerTarget(targetGraph.getPrices().get(TaskType.valueOf(task.getClassName())));
        tasksManager.addTask(task, targetGraph, createdBy, fromScratch, this::postLogs);
    }

    @Override
    public TaskManager getTaskManager() {
        return tasksManager;
    }

}



