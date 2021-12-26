import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AdjMap2 extends HashMap<String, Map<String, Boolean>> {
}

class AdjMap extends HashMap<String, Set<String>> {
}

public class TargetGraph implements Graph<Target> {

    private String GraphsName;
    private String WorkingDir;
    private AdjMap targetsAdjacentOG;
    private AdjMap targetsAdjToRunOn;
    private Map<String, Target> allTargets;
    private Map<String, Status> targetsStatuses;

    public TargetGraph() {
    }

    public TargetGraph(String GraphsName, String WorkingDir, List<Target> Targets, List<Edge> edges) {
        this.GraphsName = GraphsName;
        this.WorkingDir = WorkingDir;
        allTargets = new HashMap<>();
        targetsAdjacentOG = new AdjMap();
        targetsStatuses = new HashMap<>();
        targetsAdjToRunOn = targetsAdjacentOG;

        for (Target t : Targets) {
            this.allTargets.put(t.name, t);
            this.targetsAdjacentOG.put(t.name, new HashSet<>());

            if (t.getResult() != null)
                this.targetsStatuses.put(t.name, Status.FINISHED);
            else
                this.targetsStatuses.put(t.name, Status.FROZEN);
        }
        connect(edges);
    }

    public void connect(List<Edge> targetsEdges) {
        for (Edge e : targetsEdges) {
            targetsAdjacentOG.get(e.out).add(e.in);
            allTargets.get(e.out);
        }
    }

    public Status getStatus(String targetName) {
        return targetsStatuses.get(targetName);
    }

    public void setStatus(String targetName, Status status) {
        targetsStatuses.put(targetName, status);
    }

    public Optional<Target> getTarget(String name) {
        return Optional.ofNullable(allTargets.getOrDefault(name, null));
    }

    public boolean NoSuchTarget(String targetName) {
        return !getTarget(targetName).isPresent();
    }

    public LinkedList<List<String>> findAllPaths(String source, String destination) {
        if (NoSuchTarget(source) || NoSuchTarget(destination))
            return null;

        Map<String, Boolean> isVisited = new HashMap<>();
        for (String target : allTargets.keySet()) {
            isVisited.put(target, false);
        }
        ArrayList<String> pathList = new ArrayList<>();
        LinkedList<List<String>> allPaths = new LinkedList<>();
        pathList.add(source);
        findAllPathsRec(source, destination, isVisited, pathList, allPaths);
        return allPaths;
    }

    private void findAllPathsRec(String source, String destination, Map<String, Boolean> isVisited, List<String> localPathList, LinkedList<List<String>> allPaths) {
        if (source.equals(destination)) {
            List<String> temp = new LinkedList<>(localPathList);
            allPaths.addLast(temp);
        }

        isVisited.replace(source, true);
        for (String targetAdj : targetsAdjacentOG.get(source)) {
            if (!isVisited.get(targetAdj)) {
                localPathList.add(targetAdj);
                findAllPathsRec(targetAdj, destination, isVisited, localPathList, allPaths);
                localPathList.remove(targetAdj);
            }
        }
        isVisited.replace(source, false);
    }

    public Queue<Target> getQueueFromScratch() {
        reset();
        targetsAdjToRunOn = targetsAdjacentOG;
        return initQueue();
    }

    public Queue<Target> getQueueFromLastTime() {
        if (taskAlreadyRan()) {
            createNewGraphFromWhatsLeft();
        } else {
            targetsAdjToRunOn = targetsAdjacentOG;
        }
        return initQueue();
    }

    public Queue<Target> initQueue() {
        Queue<Target> queue = new LinkedList<>();
        targetsAdjToRunOn.keySet().forEach(t -> {
            Type type = getType(t);
            if (type == Type.leaf || type == Type.independent)
                queue.add(allTargets.get(t));
        });
        return queue;
    }

    private void reset() {
        allTargets.values().forEach(target -> {
            target.setResult(null);
            setStatus(target.name, Status.FROZEN);
        });
    }

    public void createNewGraphFromWhatsLeft() {
        targetsAdjToRunOn = new AdjMap();
        allTargets.forEach((k, v) -> {
            if (v.getResult() != Result.Success) {
                setStatus(k, Status.FROZEN);
                v.setResult(null);
                targetsAdjToRunOn.put(k, new HashSet<>());
                targetsAdjacentOG.get(k).forEach((k2) -> {
                    if (allTargets.get(k2).getResult() != Result.Success) {
                        targetsAdjToRunOn.get(k).add(k2);
                    }
                });
            }
        });
    }

    public void addTheDadsThatAllTheirSonsFinishedSuccessfullyToQueue(Queue<Target> queue, Target target) {
        requiredFor(target.name).forEach(tDadName -> { // all dads
            if (getStatus(tDadName) == Status.FROZEN) {
                AtomicBoolean AllSonsFinishedSuccessfully = new AtomicBoolean(true);
                targetsAdjacentOG.get(tDadName).forEach(tBrotherName -> {
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

    public void setFrozensToSkipped() {
        allTargets.keySet().forEach(targetName -> {
            if (getStatus(targetName).equals(Status.FROZEN))
                setStatus(targetName, Status.SKIPPED);
        });
    }

    public ArrayList<String> requiredFor(String name) {
        if (targetsAdjToRunOn == null)
            targetsAdjToRunOn = targetsAdjacentOG;

        ArrayList<String> requiredFor = new ArrayList<>();
        targetsAdjToRunOn.forEach((k, v) -> {
            if (v.contains(name))
                requiredFor.add(k);
        });
        return requiredFor;
    }

    public Type getType(String name) {
        boolean depends = !targetsAdjToRunOn.get(name).isEmpty();
        boolean required = requiredFor(name).size() > 0;
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
        LinkedList<String> path = new LinkedList<>();
        if (NoSuchTarget(vertex))
            return path;

        AdjMap temp = new AdjMap();
        for (String v : targetsAdjacentOG.keySet()) {
            temp.put(v, new HashSet<>());
        }
        path.add(vertex);
        if (findCircuitByDfs(vertex, vertex, path, temp)) {
            return path;
        }
        path.pop();
        return path;
    }

    private boolean findCircuitByDfs(String vertex, String source, LinkedList<String> path, AdjMap adjMapMark) {
        for (String neighbor : targetsAdjacentOG.get(source)) {
            if (!adjMapMark.get(source).contains(neighbor)) {
                path.add(neighbor);
                adjMapMark.get(source).add(neighbor);
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


    public Map<String, List<String>> getStatusesStatistics() {
        Map<String, List<String>> statuses = new HashMap<>();
        allTargets.values().forEach(target -> statuses.put(Engine.ifNullThenString(target.getResult(), "Skipped"), Stream.concat(statuses.getOrDefault(Engine.ifNullThenString(target.getResult(), "Skipped"), new ArrayList<>()).stream(), Stream.of(target.name)).collect(Collectors.toList())));

        return statuses;
    }

    public Map<Type, Integer> getTypesStatistics() {
        Map<Type, Integer> types = new HashMap<>();
        allTargets.values().forEach(target -> types.put(getType(target.name), types.getOrDefault(getType(target.name), 0) + 1));
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
        return "Number of targets: " + allTargets.size() + '\n' +
                "Types: " + getTypesStatistics();
    }

    public String getTargetInfo(String targetName) {
        Optional<Target> target = getTarget(targetName);
        return target.map(value -> value + "\nStatus=" + getStatus(targetName)).orElseGet(() -> targetName + " is not a target!");
    }

    public boolean taskAlreadyRan() {
        return allTargets.values().stream().anyMatch(target -> target.getResult() != null);
    }

    public void runTaskOnTarget(Target target, Task task) throws InterruptedException {
        setStatus(target.name, Status.IN_PROCESS);
        target.run(task);
        setStatus(target.name, Status.FINISHED);
    }

    @Override
    public Map<String, Set<String>> getAdjNameMap() {
        return targetsAdjacentOG;
    }

    @Override
    public Map<String, Target> getAllElementMap() {
        return allTargets;
    }
}
