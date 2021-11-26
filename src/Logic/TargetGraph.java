package Logic;

import UI.UI;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum Type {
    leaf,
    root,
    middle,
    independent
}

enum Status {
    WAITING,
    FROZEN,
    SKIPPED,
    IN_PROCESS,
    FINISHED
}

class AdjMap extends HashMap<String, Map<String, Boolean>>{}

public class TargetGraph {

    String GraphsName;
    String WorkingDir;
    AdjMap targetsAdjacentOG;
    Map<String, Target> allTargets;
    Map<String, Status> targetsStatuses;

    public TargetGraph() {}


    public TargetGraph(String GraphsName, String WorkingDir, List<Target> Targets, List<Edge> edges)  {
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

    public void setStatus(String targetName, Status status) {
        targetsStatuses.put(targetName, status);
    }

    public Optional<Target> getTarget(String name) {
        return Optional.ofNullable(allTargets.getOrDefault(name,null));
    }

    public boolean NoSuchTarget(String targetName){
        return !getTarget(targetName).isPresent();
    }

    public LinkedList<List<String>> printAllPaths(String source, String destination) {
        if( NoSuchTarget(source)|| NoSuchTarget(destination))
            return null;

        Map<String, Boolean> isVisited = new HashMap<>();
        for (String target : allTargets.keySet()) {
            isVisited.put(target, false);
        }
        ArrayList<String> pathList = new ArrayList<>();
        LinkedList<List<String>> allPaths = new LinkedList<>();
        pathList.add(source);
        return printAllPathsRec(source, destination, isVisited, pathList,allPaths)? allPaths : null;

    }
    private boolean printAllPathsRec(String source, String destination, Map<String, Boolean> isVisited, List<String> localPathList,LinkedList<List<String>> allPaths) {
        boolean flag = false;
        if (source.equals(destination)) {
            List<String> temp = new LinkedList<>(localPathList);
            allPaths.addLast(temp);
            return true;
        }

        isVisited.replace(source, true);
        for (String i : targetsAdjacentOG.get(source).keySet()) {
            if (!isVisited.get(i)) {
                localPathList.add(i);
                flag = flag || printAllPathsRec(i, destination, isVisited, localPathList,allPaths);
                localPathList.remove(i);
            }
        }
        isVisited.replace(source, false);
        return flag;
    }

    public void connect(List<Edge> targetsEdges) {
        for (Edge e : targetsEdges) {
            targetsAdjacentOG.get(e.out).put(e.in, true);
            allTargets.get(e.out);
        }
    }

    public void runTaskFromScratch(Task task) throws IOException, InterruptedException {
        reset();
        runTask(task, targetsAdjacentOG);
    }

    public void runTaskFromLastTime(Task task) throws IOException, InterruptedException {
        AdjMap targetAdj;

        if (taskAlreadyRan()) {
            targetAdj = createNewGraphFromWhatsLeft();
        } else {
            targetAdj = targetsAdjacentOG;
        }
        runTask(task, targetAdj);
    }


    private void reset() {
        allTargets.values().forEach(target -> {
            target.setResult(null);
            setStatus(target.name,Status.FROZEN);
        });
    }

    public AdjMap createNewGraphFromWhatsLeft() {
        AdjMap newTargetsAdj = new AdjMap();
        allTargets.forEach((k, v) -> {
            if (v.getResult() != Result.Success) {
                setStatus(k,Status.FROZEN);
                v.setResult(null);
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

    public void runTask(Task task, AdjMap targetAdj) throws IOException, InterruptedException {
        Queue<Target> queue = initQueue(targetAdj);

        while (!queue.isEmpty()) {
            Target target = queue.poll();
                setStatus(target.name, Status.IN_PROCESS);
                target.run(task);
                setStatus(target.name, Status.FINISHED);
            if(target.name.equals("O"))
                UI.print("J");
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
        setFrozenToSkipped();
    }

    public void setFrozenToSkipped() {
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
        LinkedList<String> path = new LinkedList<>();
        if(NoSuchTarget(vertex))
            return path;

        AdjMap temp = new AdjMap();
        for (String v : targetsAdjacentOG.keySet()) {
            temp.put(v, new HashMap<>());
        }
        path.add(vertex);
        if (findCircuitByDfs(vertex, vertex, path, temp)) {
            return path;
        }
        path.pop();
        return path;
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


    public Map<String, List<String>> getStatusesStatistics() {
        Map<String, List<String>> statuses = new HashMap<>();
        allTargets.values().forEach(target -> statuses.put(Logic.ifNullThenString(target.getResult(),"Skipped"), Stream.concat(statuses.getOrDefault(Logic.ifNullThenString(target.getResult(),"Skipped"), new ArrayList<>()).stream(),Stream.of(target.name)).collect(Collectors.toList())));

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
            res.append("    Targets Logic.Status: ").append(getStatus(target.name)).append("\n");
            res.append("    Process Time: ").append(target.getProcessTime().toString()).append("\n");
            res.append("--------------------\n");
        });
        return res.toString();
    }

    @Override
    public String toString() {
        return "Number of targets: " + allTargets.size() + '\n' +
                "Types: " + getTypesStatistics(targetsAdjacentOG);
    }

    public String getTargetInfo(String targetName) {
        Optional<Target> target = getTarget(targetName);
        return target.map(value -> value + "\nLogic.Status=" + getStatus(targetName)).orElseGet(() -> targetName + " is not a target!");
    }

    public boolean taskAlreadyRan() {
        return allTargets.values().stream().anyMatch(target -> target.getResult() != null);
    }
}
