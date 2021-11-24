
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

class AdjMap extends HashMap<String, Map<String, Boolean>> {
}

class Edge {
    String out, in;

    Edge(String depends, String required) {
        this.out = depends;
        this.in = required;
    }

    Edge(String in, String out, boolean dependsOrRequired) {
        this(dependsOrRequired ? in : out, dependsOrRequired ? out : in);
    }
}

public class TargetGraph {

    String GraphsName;
    String WorkingDir;
    AdjMap targetsAdjacentOG;
    Map<String, Target> allTargets;
    Map<String, Status> targetsStatuses;

    public TargetGraph() {}


    public TargetGraph(String GraphsName, String WorkingDir, List<Target> Targets, List<Edge> edges) throws Exception {
        this.GraphsName = GraphsName;
        this.WorkingDir = WorkingDir;
        allTargets = new HashMap<>();
        targetsAdjacentOG = new AdjMap();
        targetsStatuses = new HashMap<>();

        for (Target t : Targets) {
            this.allTargets.put(t.name, t);
            this.targetsAdjacentOG.put(t.name, new HashMap<>());

            if(t.getResult() != null)  this.targetsStatuses.put(t.name, Status.FINISHED);
            else this.targetsStatuses.put(t.name, Status.FROZEN);

        }
        connect(edges);
    }

    public Status getStatus(String targetName) {
        return targetsStatuses.get(targetName);
    }

    public Status setStatus(String targetName, Status status) {
        return targetsStatuses.put(targetName, status);
    }

    public Optional<Target> getTarget(String name) {
        return Optional.of(allTargets.getOrDefault(name,null));
    }

    public boolean validateTarget(String targetName){
        if(!getTarget(targetName).isPresent()){
            UI.error(targetName+" is not a target");
            return false;
        }
        return true;
    }

    public void printAllPaths(String source, String destination) {
        if(!validateTarget(source) || !validateTarget(destination))
            return;

        Map<String, Boolean> isVisited = new HashMap<>();
        for (String target : allTargets.keySet()) {
            isVisited.put(target, false);
        }
        ArrayList<String> pathList = new ArrayList<>();
        pathList.add(source);
        printAllPathsRec(source, destination, isVisited, pathList);
    }

    private void printAllPathsRec(String source, String destination, Map<String, Boolean> isVisited, List<String> localPathList) {
        if (source.equals(destination)) {
            UI.printPath(localPathList);
            return;
        }

        isVisited.replace(source, true);
        for (String i : targetsAdjacentOG.get(source).keySet()) {
            if (!isVisited.get(i)) {
                localPathList.add(i);
                 printAllPathsRec(i, destination, isVisited, localPathList);
                localPathList.remove(i);
            }
        }
        isVisited.replace(source, false);
    }

    public void connect(List<Edge> targetsEdges) {
        for (Edge e : targetsEdges) {
            targetsAdjacentOG.get(e.out).put(e.in, true);
            allTargets.get(e.out);
        }
    }

    public String createLogLibrary(String taskName) {
        String currentTime = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").format(LocalDateTime.now());
        String path = taskName + " - " + currentTime;
        new File(path).mkdir();
        return path;
    }

    public void log(String LibraryPath, String targetName, String loggedData) throws IOException {
        File log = new File(LibraryPath + "/" + targetName + ".log");
        log.createNewFile();
        FileWriter myWriter = new FileWriter(log.getPath());
        myWriter.write(loggedData);
        myWriter.close();
    }

    public void runTaskFromScratch(Task task) {
        reset();
        runTask(task, targetsAdjacentOG);
    }

    public void runTaskFromLastTime(Task task) {
        AdjMap targetAdj;

        if (allTargets.values().stream().anyMatch(target -> target.getResult() != null)) {
            targetAdj = createNewGraphFromWhatsLeft();
        } else {
            targetAdj = targetsAdjacentOG;
            UI.warning("target did not run yet");
        }
        runTask(task, targetAdj);
    }


    private void reset() {
        allTargets.values().forEach(target -> {
            target.setResult(null);
        });
    }

    public AdjMap createNewGraphFromWhatsLeft() {
        AdjMap newTargetsAdj = new AdjMap();
        allTargets.forEach((k, v) -> {
            if (v.getResult() != Result.Success) {
                setStatus(k,Status.FROZEN);
                newTargetsAdj.put(k, new HashMap<>());
                targetsAdjacentOG.get(k).forEach((k2, v2) -> {
                    if (allTargets.get(k2).getResult() != Result.Success) {
                        newTargetsAdj.get(k).put(k2, true);
                    }
                });
            }
        });

        return newTargetsAdj;
    }

    public Queue<Target> initQueue(AdjMap targetAdj) {
        Queue<Target> queue = new LinkedList<>();
        targetAdj.keySet().forEach(t -> {
            Type type = getType(t, targetAdj);
            if (type == Type.leaf || type == Type.independent)
                queue.add(allTargets.get(t));
        });
        return queue;
    }

    public void runTask(Task task, AdjMap targetAdj) {
        FileHandler.logLibPath = createLogLibrary(task.getName());
        Queue<Target> queue = initQueue(targetAdj);

        while (!queue.isEmpty()) {
            Target target = queue.poll();
            try {
                setStatus(target.name, Status.IN_PROCESS);
                target.run(task);
                setStatus(target.name, Status.FINISHED);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }

            requiredFor(target.name, targetAdj).forEach(tDadName -> { // all dads
                if (getStatus(tDadName) == Status.FROZEN) {
                    AtomicBoolean AllSonsFinishedSuccessfully = new AtomicBoolean(true);
                    targetsAdjacentOG.get(tDadName).keySet().forEach(tBrotherName -> {
                        Target tBrother = allTargets.get(tBrotherName);
                        if (tBrother.getResult() == Result.Failure || getStatus(tBrotherName) == Status.SKIPPED) {
                            setStatus(tBrotherName, Status.SKIPPED);
                            AllSonsFinishedSuccessfully.set(false);
                        }
                        if (getStatus(tBrotherName) != Status.FINISHED) {
                            AllSonsFinishedSuccessfully.set(false);
                        }
                    });
                    if (AllSonsFinishedSuccessfully.get()) {
                        setStatus(tDadName, Status.WAITING);
                        queue.add(allTargets.get(tDadName));
                    }
                }
            });
        }

        setFrozensToSkipped();
        UI.printDivide(getPostTaskRunInfo());
        getStatusesStatistics().forEach((k, v) -> UI.printDivide(k + ": " + v + "\n"));
    }

    public void setFrozensToSkipped() {
        allTargets.keySet().forEach(targetName -> {
            if (getStatus(targetName).equals(Status.FROZEN))
                setStatus(targetName, Status.SKIPPED);
        });
    }

    public ArrayList<String> requiredFor(String name, Map<String, Map<String, Boolean>> targetAdj) {
        if (targetAdj == null)
            targetAdj = targetsAdjacentOG;

        ArrayList<String> requiredFor = new ArrayList<>();
        targetAdj.forEach((k, v) -> {
            if (v.getOrDefault(name, false))
                requiredFor.add(k);
        });
        return requiredFor;
    }

    public Type getType(String name, AdjMap targetAdj) {
        boolean depends = !targetAdj.get(name).isEmpty();
        boolean required = requiredFor(name, targetAdj).size() > 0;
        if (depends && required) {
            return Type.middle;
        }
        if (depends) {
            return Type.root;
        }
        if (required) {
            return Type.leaf;
        }
        return Type.independent;
    }

    public LinkedList<String> findCircuit(String vertex) {
        LinkedList<String> path = new LinkedList<String>();
        if(!validateTarget(vertex))
            return path;

        AdjMap temp = new AdjMap();
        for (String v : targetsAdjacentOG.keySet()) {
            temp.put(v, new HashMap<>());
        }
        path.add(vertex);
        if (findCircuitByDfs(vertex, vertex, path, temp)) {
            return path;
        }
        return null;
    }

    private boolean findCircuitByDfs(String vertex, String source, LinkedList<String> path, AdjMap adjMapMark) {
        for (String neighbor : targetsAdjacentOG.get(source).keySet()) {
            if (!adjMapMark.get(source).containsKey(neighbor)) {
                path.add(neighbor);
                adjMapMark.get(source).put(neighbor, true);
                if (neighbor.equals(vertex)) {
                    return true;
                }
                if (findCircuitByDfs(vertex, neighbor, path, adjMapMark)) {
                    return true;
                }
                path.removeLast();
            }
        }
        return false;
    }

    public Map<String, Integer> getStatusesStatistics() {
        Map<String, Integer> statuses = new HashMap<>();
        allTargets.values().forEach(target -> statuses.put(target.getResult() != null ? target.getResult().toString() : "Skipped", statuses.getOrDefault(target.getResult() != null ? target.getResult().toString() : "Skipped", 0) + 1));

        return statuses;
    }

    public Map<Type, Integer> getTypesStatistics(AdjMap targetAdj) {
        Map<Type, Integer> types = new HashMap<>();
        allTargets.values().forEach(target -> types.put(getType(target.name, targetAdj), types.getOrDefault(getType(target.name, targetAdj), 0) + 1));
        return types;
    }

    public String getPostTaskRunInfo() {
        StringBuilder res = new StringBuilder();
        allTargets.values().forEach(target -> {
            res.append("Targets Name: ").append(target.name).append("\n");
            res.append("    Tasks Result: ").append(target.getResult()).append("\n");
            res.append("    Targets Status: ").append(getStatus(target.name)).append("\n");
            res.append("    Process Time: ").append(target.getProcessTime().toString()).append("\n");
            res.append("--------------------\n");
        });
        return res.toString();
    }

    @Override
    public String toString() {
        String res = "Number of targets: " + allTargets.size() + '\n' +
                "Types: " + getTypesStatistics(targetsAdjacentOG);
        return res;
    }

    public String getTargetInfo(String targetName) {
        if(!validateTarget(targetName))
            return "";
        return allTargets.get(targetName).toString() + "\nStatus=" + getStatus(targetName);
    }
}
