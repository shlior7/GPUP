import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class TargetGraph implements Graph<Target> {

    private String GraphsName;
    private String WorkingDir;
    private int maxThreads;
    private AdjacentMap originalTargetsGraph;
    private AdjacentMap currentTargetsGraph;
    //    private AdjMap originalTargetsGraph;
//    private AdjMap currentTargetsGraph.children;
//    private AdjMap originalTargetsGraph.parents;
    private Map<String, Target> allTargets;
    private SerialSetController serialSets;

    public TargetGraph() {
    }


    ///old console ui constructor TO DELETE!
    public TargetGraph(String GraphsName, String WorkingDir, List<Target> Targets, List<Edge> edges) {
        this.GraphsName = GraphsName;
        this.WorkingDir = WorkingDir;
        this.allTargets = new HashMap<>();
        this.originalTargetsGraph = new AdjacentMap();
        this.currentTargetsGraph = new AdjacentMap();

        this.currentTargetsGraph.clone(originalTargetsGraph);

        for (Target t : Targets) {
            this.allTargets.put(t.name, t);
            this.originalTargetsGraph.children.put(t.name, new HashSet<>());

            if (t.getResult() != Result.NULL)
                setStatus(t.name, Status.FINISHED);
            else
                setStatus(t.name, Status.FROZEN);
        }
        connect(edges);
    }

    public TargetGraph(String GraphsName, String WorkingDir, int maxThreads, List<Target> targets, List<Edge> edges, List<SerialSet> serialSets) throws Exception {
        checkValidateGraph(targets, edges, serialSets);
        this.GraphsName = GraphsName;
        this.WorkingDir = WorkingDir;
        this.maxThreads = maxThreads;
        this.allTargets = new HashMap<>();
        this.originalTargetsGraph = new AdjacentMap();
        this.currentTargetsGraph = new AdjacentMap();
        this.serialSets = new SerialSetController(serialSets);

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

    public SerialSetController getSerialSets() {
        return serialSets;
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
        for (Target targetAdj : originalTargetsGraph.children.get(source)) {
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
        System.out.println(taskAlreadyRan());
        if (taskAlreadyRan()) {
            createNewGraphFromWhatsLeft();
        }
        System.out.println(currentTargetsGraph.children);
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
//        this.currentTargetsGraph.clone(originalTargetsGraph);
    }

    public void createNewGraphFromWhatsLeft() {
        AdjacentMap newGraph = new AdjacentMap();
        currentTargetsGraph.children.keySet().forEach((name) -> {
            Target target = allTargets.get(name);
            System.out.println(target.getName() + "  " + target.getResult());

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
//        createTargetsGraphInfo(newGraph.children.keySet().stream().map(name -> allTargets.get(name)).collect(Collectors.toSet()));
        currentTargetsGraph.clone(newGraph);
        System.out.println(currentTargetsGraph.children);
        System.out.println("---------------------");
        System.out.println(newGraph.children);
    }

    public boolean createNewGraphFromTargetList(Set<Target> targetsToRunOn) {
        boolean CurrentGraphContainsTargetsToRunOn = getCurrentTargets().containsAll(targetsToRunOn);

        if (targetsToRunOn.size() == originalTargetsGraph.children.size()) {
            currentTargetsGraph.children = originalTargetsGraph.children;
            return CurrentGraphContainsTargetsToRunOn;
        }

        currentTargetsGraph = new AdjacentMap();
        targetsToRunOn.forEach(((target) -> {
            target.setTargetInfo(createTargetInGraphInfo(target));
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

        System.out.println(getResultStatistics());
        return CurrentGraphContainsTargetsToRunOn;
    }


    public void setFrozensToSkipped() {
        allTargets.keySet().forEach(targetName -> {
            if (getStatus(targetName).equals(Status.FROZEN))
                setStatus(targetName, Status.SKIPPED);
        });
    }

    public Set<Target> whoAreYourDirectDaddies(String name) {
        return currentTargetsGraph.parents.get(name);
    }

    public Set<Target> whoAreYourDirectBabies(String name) {
        return currentTargetsGraph.children.get(name);
    }


    public Set<Target> whoAreYourAllDaddies(String name) {
        return recursiveChildrenCounting(name, currentTargetsGraph.parents);
    }

    public Set<Target> whoAreYourAllBabies(String name) {
        return recursiveChildrenCounting(name, currentTargetsGraph.children);
    }


    public Type getType(String name) {
        if (!currentTargetsGraph.children.containsKey(name))
            return null;
        boolean depends = !currentTargetsGraph.children.get(name).isEmpty();
        boolean required = whoAreYourDirectDaddies(name).size() > 0;
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

    public Map<Type, Integer> getTypesStatistics() {
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
                "Types: " + getTypesStatistics();
    }

    public void setParentsStatuses(String targetName, Status status, AtomicInteger TargetsDone) {
        currentTargetsGraph.parents.get(targetName).stream().parallel().forEach((target -> {
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
        return target.map(value -> value + "\nStatus=" + getStatus(targetName)).orElseGet(() -> targetName + " is not a target!");
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

    public String createTargetInGraphInfo(Target target) {
        Set<Target> parents = whoAreYourAllDaddies(target.name);
        Set<Target> babies = whoAreYourAllBabies(target.name);

        String res = "\nType: " + getIfNotInGraph(getType(target.name)) +
                "\nDepends on: " + babies.size() + (babies.size() == 0 ? "" : " - " + whoAreYourAllBabies(target.name)) +
                "\nRequired For: " + parents.size() + (parents.size() == 0 ? "" : " - " + whoAreYourAllDaddies(target.name));
        return res;
    }

    public void createTargetsGraphInfo(Set<Target> targetsToSet) {
        targetsToSet.forEach(target -> target.setTargetInfo(createTargetInGraphInfo(target)));
    }

    @Override
    public String getVertexInfo(Target target) {
        if (getType(target.name) == null) {
            return "Name: " + target.name +
                    "\nUser Data: " + target.getUserData() +
                    "\nSerial Sets: " + serialSets.getSerialSetsOf(target.name).size();
        }
        if (target.getTargetInfo() == null) {
            target.setTargetInfo(createTargetInGraphInfo(target));
        }

        return target.geStringInfo() + target.getTargetInfo();
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
//        return requiredOn.get();
    }

    public void printGraphStatusInfo() {
        getStatusesStatistics().forEach((k, v) -> System.out.println(k + ": " + v.size() + " : {" + String.join(", ", v) + "}" + "\n"));

    }

    public void printStatsInfo(Map<String, List<String>> stats) {
        stats.forEach((k, v) -> System.out.println(k + ": " + v.size() + " : {" + String.join(", ", v) + "}" + "\n"));
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

    public void checkValidateGraph(List<Target> targets, List<Edge> edges, List<SerialSet> serialSets) throws Exception {
        checkEqualTargets(targets);
        checkValidEdges(targets, edges);
        checkSerialSets(targets, serialSets);
    }

    public void checkSerialSets(List<Target> targets, List<SerialSet> serialSets) throws Exception {
        Map<String, String> allTargetsInSerialMap = new HashMap<>();
        for (SerialSet serialSet : serialSets) {
            HashSet<String> serial = serialSet.getTargets();
            for (String target : serial) {
                allTargetsInSerialMap.put(target, serialSet.getName());
            }
        }
        Map<String, Integer> duplicates = new HashMap<>();
        serialSets.stream().map(SerialSet::getName).forEach(name ->
                duplicates.put(name, duplicates.getOrDefault(name, 0) + 1)
        );

        for (Map.Entry<String, Integer> entry : duplicates.entrySet()) {
            if (entry.getValue() > 1)
                throw new Exception("there are 2 serial sets with the same name ( " + entry.getKey() + " )");
        }

        for (SerialSet serialSet1 : serialSets) {
            for (SerialSet serialSet2 : serialSets) {
                if (serialSet1 != serialSet2) {
                    if (serialSet1.getName().equals(serialSet2.getName())) {
                        throw new Exception("there are 2 serial sets with the same name ( " + serialSet1.getName() + " )");
                    }
                }
            }
        }
        AtomicBoolean equal = new AtomicBoolean(false);
        for (Map.Entry<String, String> entry : allTargetsInSerialMap.entrySet()) {
            for (Target target : targets) {
                if (target.name.equals(entry.getKey()))
                    equal.set(true);
            }
            if (!equal.get()) {
                throw new Exception("The target \"" + entry.getKey() + "\" in serial set \"" + entry.getValue() + "\" doesn't exist");
            }
            equal.set(false);
        }
    }

    public void checkValidEdges(List<Target> targets, List<Edge> edges) throws Exception {
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
                throw new Exception("Target " + edge.out + " does not exist");
            }
            if (!validIn.get()) {
                throw new Exception("Target " + edge.in + " does not exist");
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
        return "Targets Count: " + totalSize() + " \n" + getTypesStatistics().toString() + "\n Serial Sets: \n" + String.join("\n", serialSets.getSerialSets());
    }
}
