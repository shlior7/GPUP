package TargetGraph;

import graph.Graph;
import types.Admin;

import utils.RecursiveConsumer;
import utils.Utils;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TargetGraph implements Graph<Target> {
    private Admin createdBy;
    private String GraphsName;
    private String WorkingDir;
    private int maxThreads;
    private AdjacentMap originalTargetsGraph;
    private AdjacentMap currentTargetsGraph;
    private Map<String, Target> allTargets;
    private Map<Type, Integer> typesStatistics;

    public TargetGraph() {
    }

    public TargetGraph(String GraphsName, String WorkingDir, int maxThreads, List<Target> targets, List<Edge> edges) throws Exception {
        validateGraph(targets, edges);
        this.GraphsName = GraphsName;
        this.WorkingDir = WorkingDir;
        this.maxThreads = maxThreads;
        this.allTargets = new HashMap<>();
        this.originalTargetsGraph = new AdjacentMap();
        this.currentTargetsGraph = new AdjacentMap();
        this.currentTargetsGraph.clone(originalTargetsGraph);

        targets.forEach(target -> {
            this.allTargets.put(target.name, target);
            this.originalTargetsGraph.children.put(target.name, new HashSet<>());
            this.originalTargetsGraph.parents.put(target.name, new HashSet<>());
            if (target.getResult() != Result.NULL)
                setStatus(target.name, Status.FINISHED);
            else
                setStatus(target.name, Status.FROZEN);
        });
        connect(edges);
        this.typesStatistics = getCurrentTypesStatistics();
    }

    public void connect(List<Edge> targetsEdges) {
        for (Edge e : targetsEdges) {
            originalTargetsGraph.children.get(e.out).add(allTargets.get(e.in));
            originalTargetsGraph.parents.get(e.in).add(allTargets.get(e.out));
        }
    }

    public int totalSize() {
        return allTargets.size();
    }

    public int size() {
        return currentTargetsGraph.children.size();
    }

    public Status getStatus(String targetName) {
        return allTargets.get(targetName).getStatus();
    }

    public void setStatus(String targetName, Status status) {
        allTargets.get(targetName).setStatus(status);
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
        for (Target targetAdj : whoAreYourDirectBabies(source)) {
            if (!isVisited.get(targetAdj.name)) {
                localPathList.add(targetAdj.name);
                findAllPathsRec(targetAdj.name, destination, isVisited, localPathList, allPaths);
                localPathList.remove(targetAdj.name);
            }
        }
        isVisited.replace(source, false);
    }

    public Queue<Target> getQueueFromScratch() {
        reset();
        currentTargetsGraph.children = originalTargetsGraph.children;
        return initQueue();
    }

    public Queue<Target> getQueueFromLastTime() {
        if (taskAlreadyRan()) {
            createNewGraphFromWhatsLeft();
        }
        return initQueue();
    }

    public Queue<Target> initQueue() {
        Queue<Target> queue = new LinkedList<>();
        currentTargetsGraph.children.keySet().forEach(t -> {
            Type type = getType(t);
            if (type == Type.leaf || type == Type.independent)
                queue.add(allTargets.get(t));
        });
        return queue;
    }

    public void reset() {
        allTargets.values().forEach(target -> {
            target.setResultFromStr(null);
            target.setTargetInfo(null);
            setStatus(target.name, Status.FROZEN);
        });
    }

    public void createNewGraphFromWhatsLeft() {
        AdjacentMap newGraph = new AdjacentMap();
        currentTargetsGraph.children.keySet().forEach((name) -> {
            Target target = allTargets.get(name);

            newGraph.children.put(name, new HashSet<>());
            newGraph.parents.putIfAbsent(name, new HashSet<>());

            if (target.getResult() != Result.Success && target.getResult() != Result.Warning)
                target.init(createTargetInGraphInfo(target));

            currentTargetsGraph.children.get(name).forEach((child) -> {
                newGraph.children.get(name).add(child);
                newGraph.parents.putIfAbsent(child.name, new HashSet<>());
                newGraph.parents.get(child.name).add(target);
            });
        });
        currentTargetsGraph.clone(newGraph);
    }

    public boolean createNewGraphFromTargetList(Set<Target> targetsToRunOn) {
        boolean CurrentGraphContainsTargetsToRunOn = getCurrentTargets().containsAll(targetsToRunOn);

        if (targetsToRunOn.size() == originalTargetsGraph.children.size()) {
            currentTargetsGraph.children = originalTargetsGraph.children;
            return CurrentGraphContainsTargetsToRunOn;
        }

        currentTargetsGraph = new AdjacentMap();
        targetsToRunOn.forEach(((target) -> {
            currentTargetsGraph.children.put(target.name, new HashSet<>());
            currentTargetsGraph.parents.putIfAbsent(target.name, new HashSet<>());

            originalTargetsGraph.children.get(target.name).forEach((k2) -> {
                if (targetsToRunOn.contains(k2)) {
                    currentTargetsGraph.children.get(target.name).add(k2);
                    currentTargetsGraph.parents.putIfAbsent(k2.name, new HashSet<>());
                    currentTargetsGraph.parents.get(k2.name).add(target);
                }
            });
        }));
        createTargetsGraphInfo(targetsToRunOn);

        return CurrentGraphContainsTargetsToRunOn;
    }


    public void setFrozensToSkipped() {
        allTargets.keySet().forEach(targetName -> {
            if (getStatus(targetName).equals(Status.FROZEN))
                setStatus(targetName, Status.SKIPPED);
        });
    }

    public Set<Target> whoAreYourDirectDaddies(String name) {
        return currentTargetsGraph.parents.getOrDefault(name, new HashSet<>());
    }

    public Set<Target> whoAreYourDirectBabies(String name) {
        return currentTargetsGraph.children.getOrDefault(name, new HashSet<>());
    }


    public Set<Target> whoAreAllYourDaddies(String name) {
        return recursiveChildrenCounting(name, currentTargetsGraph.parents);
    }

    public Set<Target> whoAreAllYourBabies(String name) {
        return recursiveChildrenCounting(name, currentTargetsGraph.children);
    }


    public Type getType(String name) {
        if (!currentTargetsGraph.children.containsKey(name))
            return null;
        boolean depends = !whoAreYourDirectBabies(name).isEmpty();
        boolean required = !whoAreYourDirectDaddies(name).isEmpty();
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
        for (String v : originalTargetsGraph.children.keySet()) {
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
        for (Target neighbor : originalTargetsGraph.children.get(source)) {
            if (!adjMapMark.get(source).contains(neighbor)) {
                path.add(neighbor.name);
                adjMapMark.get(source).add(neighbor);
                if (neighbor.name.equals(vertex)) {
                    return true;
                }
                if (findCircuitByDfs(vertex, neighbor.name, path, adjMapMark)) {
                    return true;
                }
                path.removeLast();
            }
        }
        return false;
    }


    public Map<String, List<String>> getStatusesStatistics() {
        Map<String, List<String>> statuses = new HashMap<>();
        allTargets.values().forEach(target -> statuses.put(target.getStatus().name(), Stream.concat(statuses.getOrDefault(target.getStatus().name(), new ArrayList<>()).stream(), Stream.of(target.name)).collect(Collectors.toList())));

        return statuses;
    }

    public Map<String, List<String>> getResultStatistics() {
        Map<String, List<String>> statuses = new HashMap<>();
        getCurrentTargets().forEach(target -> statuses.put(target.getResult() == Result.NULL ? "Skipped" : target.getResult().toString(), Stream.concat(statuses.getOrDefault(target.getResult() == Result.NULL ? "Skipped" : target.getResult().toString(), new ArrayList<>()).stream(), Stream.of(target.name)).collect(Collectors.toList())));

        return statuses;
    }

    public Map<Type, Integer> getCurrentTypesStatistics() {
        Map<Type, Integer> types = new HashMap<>();
        allTargets.values().forEach(target -> types.put(getType(target.name), types.getOrDefault(getType(target.name), 0) + 1));
        return types;
    }


    public Set<Target> getCurrentTargets() {
        return currentTargetsGraph.children.keySet().stream().map(name -> allTargets.get(name)).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return "Number of targets: " + allTargets.size() + '\n' +
                "Types: " + getCurrentTypesStatistics();
    }

    public void setParentsStatuses(String targetName, Status status, AtomicInteger TargetsDone) {
        whoAreYourDirectDaddies(targetName).stream().parallel().forEach((target -> {
            if (target.getStatus() == status)
                return;
            target.setStatus(status);
            TargetsDone.incrementAndGet();
            setParentsStatuses(target.name, status, TargetsDone);
        }));
    }

    public boolean didAllChildrenFinish(String targetName) {
        return whoAreYourDirectBabies(targetName).stream().allMatch(tChild -> tChild.getStatus() == Status.FINISHED);
    }

    public String getTargetInfo(String targetName) {
        Optional<Target> target = getTarget(targetName);
        return target.map(value -> value + "\nTargetGraph.Status=" + getStatus(targetName)).orElseGet(() -> targetName + " is not a target!");
    }

    public boolean taskAlreadyRan() {
        return getCurrentTargets().stream().anyMatch(target -> target.getResult() != Result.NULL);
    }


    @Override
    public Map<String, Set<Target>> getAdjacentNameMap() {
        return currentTargetsGraph.children;
    }

    @Override
    public Map<String, Target> getAllElementMap() {
        return allTargets;
    }

    public String getWhySkipped(String name) {
        for (Target child : whoAreYourDirectBabies(name)) {
            if (child.getResult() == Result.Failure) {
                return child.getName();
            } else if (child.getStatus() == Status.SKIPPED) {
                return child.getName() + " ->" + getWhySkipped(child.getName());
            }
        }
        return "";
    }

    public String createStatusInfo(Target target) {
        switch (target.getStatus()) {
            case WAITING:
                return "\n Waiting For " + Duration.between(target.getWaitingTime(), Instant.now()).toMillis();
            case FROZEN:
                return "\n Waiting For " + whoAreAllYourBabies(target.name).stream().filter(t -> t.getStatus() != Status.FINISHED).collect(Collectors.toList());
            case SKIPPED:
                return "\n Skipped because { " + getWhySkipped(target.name) + " }";
            case IN_PROCESS:
                return "\n Time it's Processing " + Duration.between(target.getStartedTime(), Instant.now()).toMillis();
            case FINISHED:
                return "\n Process time " + target.getProcessTime().toMillis();
        }
        return "";
    }

    public String createTargetInGraphInfo(Target target) {
        Set<Target> parents = whoAreAllYourDaddies(target.name);
        Set<Target> babies = whoAreAllYourBabies(target.name);


        String res = "\nType: " + getIfNotInGraph(getType(target.name)) +
                "\nDepends on: " + babies.size() + (babies.size() == 0 ? "" : " - " + whoAreAllYourBabies(target.name)) +
                "\nRequired For: " + parents.size() + (parents.size() == 0 ? "" : " - " + whoAreAllYourDaddies(target.name));

        return res;
    }

    public void createTargetsGraphInfo(Set<Target> targetsToSet) {
        targetsToSet.forEach(target -> target.setTargetInfo(createTargetInGraphInfo(target)));
    }

    @Override
    public String getVertexInfo(Target target) {
        if (getType(target.name) == null) {
            return "Name: " + target.name +
                    "\nUser Data: " + target.getUserData();
        }
        if (target.getTargetInfo() == null) {
            target.setTargetInfo(createTargetInGraphInfo(target));
        }
        return target.geStringInfo() + target.getTargetInfo() + (isGraphRunning() ? createStatusInfo(target) : "");
    }

    public boolean isGraphRunning() {
        return getCurrentTargets().stream().anyMatch(t -> t.getStatus() != Status.FROZEN);
    }

    public Object getIfNotInGraph(Object object) {
        return Utils.getIfNullDefault(object, "NOT_IN_GRAPH");
    }

    public int howManyDependsOn(String name) {
        return recursiveChildrenCounting(name, currentTargetsGraph.children).size();
    }

    public int howManyRequireFor(String name) {
        return recursiveChildrenCounting(name, currentTargetsGraph.parents).size();
    }

    private Set<Target> recursiveChildrenCounting(String name, AdjMap adjacentMap) {
        Map<String, Boolean> passedOn = new HashMap<>(allTargets.size());
        AtomicInteger requiredOn = new AtomicInteger(0);
        Set<Target> children = new HashSet<>();

        RecursiveConsumer<String> rec = (consumer, targetName) -> adjacentMap.getOrDefault(targetName, new HashSet<>()).forEach((target -> {
            if (Boolean.TRUE.equals(passedOn.putIfAbsent(target.name, false)))
                return;
            synchronized (this) {
                passedOn.put(target.name, true);
                requiredOn.incrementAndGet();
                children.add(target);
            }
            consumer.accept(target.name);
        }));

        rec.accept(name);
        return children;
    }


    public String getStatsInfoString(Map<String, List<String>> statsInfo) {
        return "\n" + statsInfo.keySet().stream()
                .map(key -> key + " " + statsInfo.get(key).size() + " : { " + String.join(", ", statsInfo.get(key)) + " }")
                .collect(Collectors.joining("\n"));
    }


    public int getMaxThreads() {
        return maxThreads;
    }

    public String getWorkingDir() {
        return WorkingDir;
    }

    public void validateGraph(List<Target> targets, List<Edge> edges) throws Exception {
        checkEqualTargets(targets);
        validateEdges(targets, edges);
    }

    public void validateEdges(List<Target> targets, List<Edge> edges) throws Exception {
        AtomicBoolean validIn = new AtomicBoolean(false);
        AtomicBoolean validOut = new AtomicBoolean(false);
        for (Edge edge : edges) {
            for (Target target : targets) {
                if (target.name.equals(edge.in)) {
                    validIn.set(true);
                }
                if (target.name.equals(edge.out)) {
                    validOut.set(true);
                }
            }
            if (!validOut.get()) {
                throw new Exception("TargetGraph.Target " + edge.out + " does not exist");
            }
            if (!validIn.get()) {
                throw new Exception("TargetGraph.Target " + edge.in + " does not exist");
            }
            validIn.set(false);
            validOut.set(false);
            checkConflictBetweenDependencies(edge, edges);
        }
    }

    public void checkConflictBetweenDependencies(Edge edge, List<Edge> edges) throws Exception {
        for (Edge edge1 : edges) {
            if (edge.in.equals(edge1.out) && edge.out.equals(edge1.in)) {
                throw new Exception("There is a conflict between dependencies of " + edge.in + " and " + edge.out);
            }
        }
    }


    public void checkEqualTargets(List<Target> targets) throws Exception {
        Map<String, Integer> duplicates = new HashMap<>();
        targets.stream().map(Target::getName).forEach(name ->
                duplicates.put(name, duplicates.getOrDefault(name, 0) + 1)
        );
        if (duplicates.values().stream().anyMatch(count -> count > 1))
            throw new Exception("There are 2 targets with the same name");
    }

    public String getInfo() {
        return "Targets Count: " + totalSize() + " \n" + getCurrentTypesStatistics().toString() + "\n Serial Sets: \n" + String.join("\n");
    }

    public String getGraphsName() {
        return GraphsName;
    }

    public void setCreatedBy(Admin createdBy) {
        this.createdBy = createdBy;
    }

    public Admin getCreatedBy() {
        return this.createdBy;
    }

    public Map<Type, Integer> getTypesStatistics() {
        System.out.println(typesStatistics);
        System.out.println(getCurrentTypesStatistics());
        return typesStatistics;
    }

    public void setTypesStatistics(Map<Type, Integer> typesStatistics) {
        this.typesStatistics = typesStatistics;
    }
}
