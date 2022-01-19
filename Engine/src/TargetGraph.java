import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class TargetGraph implements Graph<Target> {

    private String GraphsName;
    private String WorkingDir;
    private int maxParallelism;
    private AdjMap targetsAdjacentOG;
    private AdjMap targetsAdjToRunOn;
    private AdjMap parentsMap;
    private Map<String, Target> allTargets;
    private SerialSetController serialSets;
//    public ExecutorService threadExecutor;

    public TargetGraph() {
    }

    public TargetGraph(String GraphsName, String WorkingDir, List<Target> Targets, List<Edge> edges) {
        this.GraphsName = GraphsName;
        this.WorkingDir = WorkingDir;
        this.allTargets = new HashMap<>();
        this.targetsAdjacentOG = new AdjMap();
        this.targetsAdjToRunOn = targetsAdjacentOG;

        for (Target t : Targets) {
            this.allTargets.put(t.name, t);
            this.targetsAdjacentOG.put(t.name, new HashSet<>());

            if (t.getResult() != Result.NULL)
                setStatus(t.name, Status.FINISHED);
            else
                setStatus(t.name, Status.FROZEN);
        }
        connect(edges);
    }

    public TargetGraph(String GraphsName, String WorkingDir, int maxParallelism, List<Target> Targets, List<Edge> edges, List<SerialSet> serialSets) {
        this.GraphsName = GraphsName;
        this.WorkingDir = WorkingDir;
        this.maxParallelism = maxParallelism;
        this.allTargets = new HashMap<>();
        this.targetsAdjacentOG = new AdjMap();
        this.parentsMap = new AdjMap();
        this.targetsAdjToRunOn = targetsAdjacentOG;
        this.serialSets = new SerialSetController(serialSets);

        for (Target t : Targets) {
            this.allTargets.put(t.name, t);
            this.targetsAdjacentOG.put(t.name, new HashSet<>());
            this.parentsMap.put(t.name, new HashSet<>());

            if (t.getResult() != Result.NULL)
                setStatus(t.name, Status.FINISHED);
            else
                setStatus(t.name, Status.FROZEN);
        }
        connect(edges);
    }

    public void connect(List<Edge> targetsEdges) {
        for (Edge e : targetsEdges) {
            targetsAdjacentOG.get(e.out).add(allTargets.get(e.in));
            parentsMap.get(e.in).add(allTargets.get(e.out));
        }
    }

    public int size() {
        return allTargets.size();
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
        for (Target targetAdj : targetsAdjacentOG.get(source)) {
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
            target.setResultFromStr(null);
            setStatus(target.name, Status.FROZEN);
        });
    }

    public void createNewGraphFromWhatsLeft() {
        targetsAdjToRunOn = new AdjMap();
        allTargets.forEach((k, target) -> {
            if (target.getResult() == Result.Success)
                return;

            target.setResultFromStr(null);
            target.setStatus(Status.FROZEN);
            targetsAdjToRunOn.put(k, new HashSet<>());
            targetsAdjacentOG.get(k).forEach((k2) -> {
                if (k2.getResult() != Result.Success) {
                    targetsAdjToRunOn.get(k).add(k2);
                    parentsMap.get(k2.name).add(target);
                }
            });
        });
    }

//    public void addTheDadsThatAllTheirSonsFinishedSuccessfullyToQueue(Queue<Target> queue, Target target) {
//        whoAreYourDaddies(target.name).forEach(tDadName -> { // all dads
//            if (getStatus(tDadName) == Status.FROZEN) {
//                AtomicBoolean AllSonsFinishedSuccessfully = new AtomicBoolean(true);
//                targetsAdjacentOG.get(tDadName).forEach(tBrotherName -> {
//                    Target tBrother = allTargets.get(tBrotherName);
//
//                    if (tBrother.getResult() == Result.Failure || getStatus(tBrotherName) == Status.SKIPPED) {
//                        setStatus(tBrotherName, Status.SKIPPED);
//                        AllSonsFinishedSuccessfully.set(false);
//                    }
//
//                    if (getStatus(tBrotherName) != Status.FINISHED) {
//                        AllSonsFinishedSuccessfully.set(false);
//                    }
//                });
//
//                if (AllSonsFinishedSuccessfully.get()) {
//                    setStatus(tDadName, Status.WAITING);
//                    queue.add(allTargets.get(tDadName));
//                }
//            }
//        });
//    }

//    public void runTask(Task task, boolean startFromLastPoint) throws InterruptedException {
//        Queue<Target> queue;
//        if (startFromLastPoint) {
//            queue = getQueueFromLastTime();
//        } else {
//            queue = getQueueFromScratch();
//        }
//
//        while (!queue.isEmpty()) {
//            Target target = queue.poll();
//            runTaskOnTarget(target, new Simulation((Simulation) task));
//        }
////        threadExecutor.shutdown();
////        boolean ok = threadExecutor.awaitTermination(3000, TimeUnit.SECONDS);
////        System.out.println(ok);
//        getStatusesStatistics().forEach((k, v) -> System.out.println(k + ": " + v.size() + " : {" + String.join(", ", v) + "}" + "\n"));
//    }


//
//    public void runTheDadsThatAllTheirSonsFinishedSuccessfully(Target target, Task task) {
//        System.out.println("searching parents target = " + target + ", task = " + task);
//        requiredFor(target.name).forEach(tDadName -> { // all dads
//            if (getStatus(tDadName) != Status.FROZEN) {
//                return;
//            }
//            if (targetsAdjacentOG.get(tDadName).stream().anyMatch(tBrotherName -> getStatus(tBrotherName) != Status.FINISHED)) {
//                return;
//            }
//
//            AtomicBoolean AllSonsFinishedSuccessfully = new AtomicBoolean(true);
//            boolean allSonsFinished = targetsAdjacentOG.get(tDadName).stream().anyMatch(tBrotherName -> allTargets.get(tBrotherName).getResult() == Result.Failure);
//            targetsAdjacentOG.get(tDadName).forEach(tBrotherName -> {
//                Target tBrother = allTargets.get(tBrotherName);
//
//                if (tBrother.getResult() == Result.Failure || getStatus(tBrotherName) == Status.SKIPPED) {
//                    setStatus(tDadName, Status.SKIPPED);
//                    AllSonsFinishedSuccessfully.set(false);
//                }
//            });
//            System.out.println(tDadName + " , " + AllSonsFinishedSuccessfully.get());
//            if (AllSonsFinishedSuccessfully.get()) {
//                setStatus(tDadName, Status.WAITING);
//                Target d = allTargets.get(tDadName);
//                try {
//                    runTaskOnTarget(allTargets.get(tDadName), new Simulation((Simulation) task));
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
////                    queue.add(allTargets.get(tDadName));
//            }
//        });
//    }

    public void setFrozensToSkipped() {
        allTargets.keySet().forEach(targetName -> {
            if (getStatus(targetName).equals(Status.FROZEN))
                setStatus(targetName, Status.SKIPPED);
        });
    }

    public Set<Target> whoAreYourDaddies(String name) {
        return parentsMap.get(name);
    }

    public Set<Target> whoAreYourBabies(String name) {
        return targetsAdjToRunOn.get(name);
    }

    public Type getType(String name) {
        boolean depends = !targetsAdjToRunOn.get(name).isEmpty();
        boolean required = whoAreYourDaddies(name).size() > 0;
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
        for (Target neighbor : targetsAdjacentOG.get(source)) {
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
//        allTargets.values().forEach(target -> statuses.put(Engine.ifNullThenString(target.getResult(), "Skipped"), Stream.concat(statuses.getOrDefault(Engine.ifNullThenString(target.getResult(), "Skipped"), new ArrayList<>()).stream(), Stream.of(target.name)).collect(Collectors.toList())));
        allTargets.values().forEach(target -> statuses.put(target.getStatus().name(), Stream.concat(statuses.getOrDefault(target.getStatus().name(), new ArrayList<>()).stream(), Stream.of(target.name)).collect(Collectors.toList())));

        return statuses;
    }

    public Map<String, List<String>> getResultStatistics() {
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

    public void setParentsStatuses(String targetName, Status status, AtomicInteger targetsDone) {
        parentsMap.get(targetName).stream().parallel().forEach((target -> {
            if (target.getStatus() == status)
                return;
            target.setStatus(status);
            synchronized (this) {
                targetsDone.incrementAndGet();
                System.out.println("++++++++++++++++++++++++++++++++");
                System.out.println("target: " + target.name + " was set to skipped!!!!" + targetsDone.get());
                System.out.println("++++++++++++++++++++++++++++++++");
            }
            setParentsStatuses(target.name, status, targetsDone);
        }));
    }

    public boolean didAllChildrenFinish(String targetName) {
        return whoAreYourBabies(targetName).stream().allMatch(tChild -> tChild.getStatus() == Status.FINISHED);
    }

    public String getTargetInfo(String targetName) {
        Optional<Target> target = getTarget(targetName);
        return target.map(value -> value + "\nStatus=" + getStatus(targetName)).orElseGet(() -> targetName + " is not a target!");
    }

    public boolean taskAlreadyRan() {
        return allTargets.values().stream().anyMatch(target -> target.getResult() != null);
    }


    @Override
    public Map<String, Set<Target>> getAdjNameMap() {
        return targetsAdjToRunOn;
    }

    @Override
    public Map<String, Target> getAllElementMap() {
        return allTargets;
    }

    @Override
    public String getVertexInfo(Target target) {
        String res = target.geStringInfo() + "\nType: " + getType(target.name) +
                "\nDepends on: " + howManyDependsOn(target.name) +
                "\nRequired For: " + howManyRequireFor(target.name);
        return res;
    }

    public int howManyDependsOn(String name) {
        return recursiveChildrenCounting(name, targetsAdjToRunOn);
    }

    public int howManyRequireFor(String name) {
        return recursiveChildrenCounting(name, parentsMap);
    }

    private int recursiveChildrenCounting(String name, AdjMap parentsMap) {
        Map<String, Boolean> passedOn = new HashMap<>(allTargets.size());
        AtomicInteger requiredOn = new AtomicInteger(0);

        RecursiveConsumer<String> rec = (consumer, targetName) -> parentsMap.get(targetName).forEach((target -> {
            if (Boolean.TRUE.equals(passedOn.putIfAbsent(target.name, false)))
                return;
            synchronized (this) {
                passedOn.put(target.name, true);
                requiredOn.incrementAndGet();
            }
            consumer.accept(target.name);
        }));

        rec.accept(name);

        return requiredOn.get();
    }

    public void printGraphStatusInfo() {
        getStatusesStatistics().forEach((k, v) -> System.out.println(k + ": " + v.size() + " : {" + String.join(", ", v) + "}" + "\n"));

    }

    public void printStatsInfo(Map<String, List<String>> stats) {
        stats.forEach((k, v) -> System.out.println(k + ": " + v.size() + " : {" + String.join(", ", v) + "}" + "\n"));

    }

}
