import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public TargetGraph() {
    }

    public Target getTarget(String name) {
        return allTargets.get(name);

    }

    public void printAllPaths(String source, String destination)
    {
        Map<String,Boolean> isVisited = new HashMap<>();
        for (String target:allTargets.keySet()) {
            isVisited.put(target,false);
        }
        ArrayList<String> pathList = new ArrayList<>();
        pathList.add(source);
        printAllPathsRec(source, destination, isVisited, pathList);
    }

    private void printPath(List<String> PathList){
        for (String u:PathList) {
            UI.print(u+ "->");
        }
    }

    private void printAllPathsRec(String u, String d, Map<String,Boolean> isVisited, List<String> localPathList)
    {
        if (u.equals(d)) {
            printPath(localPathList);
            return;
        }
        isVisited.replace(u,true);
        for (String i : targetsAdjacentOG.get(u).keySet()) {
            if (!isVisited.get(i)) {
                localPathList.add(i);
                printAllPathsRec(i, d, isVisited, localPathList);
                localPathList.remove(i);
            }
        }
        isVisited.replace(u,false);
    }

    public TargetGraph(String GraphsName, String WorkingDir, List<Target> Targets, List<Edge> edges) throws Exception {
        this.GraphsName = GraphsName;
        this.WorkingDir = WorkingDir;
        allTargets = new HashMap<>();
        targetsAdjacentOG = new AdjMap();
        for (Target t : Targets) {
            this.allTargets.put(t.name, t);
            this.targetsAdjacentOG.put(t.name, new HashMap<>());
        }
        connect(edges);
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
        AdjMap targetAdj = createNewGraphFromWhatsLeft();
        runTask(task, targetAdj);
    }


    private void reset() {

    }

    public AdjMap createNewGraphFromWhatsLeft() {
        AdjMap newTargetsAdj = new AdjMap();
        allTargets.forEach((k, v) -> {
            if (v.getResult() != Result.Success) {
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

    public void runTask(@NotNull Task task, AdjMap targetAdj) {
        Logger.libPath = createLogLibrary(task.getName());
        Queue<Target> queue = initQueue(targetAdj);

        while (!queue.isEmpty()) {
            Target target = queue.poll();
            try {
                target.run(task);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }

            requiredFor(target.name, targetAdj).forEach(tDad -> { // all dads
                if (tDad.getStatus() == Status.FROZEN) {
                    AtomicBoolean AllSonsFinishedSuccessfully = new AtomicBoolean(true);
                    targetsAdjacentOG.get(tDad.name).keySet().stream().forEach(tBrotherName -> {
                        Target tBrother = allTargets.get(tBrotherName);
                        if (tBrother.getResult() == Result.Failure || tBrother.getStatus() == Status.SKIPPED) {
                            tDad.setStatus(Status.SKIPPED);
                            AllSonsFinishedSuccessfully.set(false);
                        }
                        if (tBrother.getStatus() != Status.FINISHED) {
                            AllSonsFinishedSuccessfully.set(false);
                        }
                    });
                    if (AllSonsFinishedSuccessfully.get()) {
                        tDad.setStatus(Status.WAITING);
                        queue.add(tDad);
                    }
                }

                ///if all of the sons of t's dad finished succsefully add to queue if any of them failed t's dad is skipped
                //
//                if (targetsAdj.get(tDad.name).keySet().stream().allMatch(tc -> allTargets.get(tc).status.isFinished() && !allTargets.get(tc).status.DidFailed()))
//                    queue.add(tDad);
            });
        }

        setFrozensToSkipped();
        UI.print(getPostTaskRunInfo());
        getStatusesStatistics().forEach((k, v) -> UI.print(k + ": " + v + "\n"));
    }

    public void setFrozensToSkipped() {
        allTargets.values().forEach(target -> {
            if (target.getStatus().equals(Status.FROZEN))
                target.setStatus(Status.SKIPPED);
        });
    }

    public ArrayList<Target> requiredFor(String name, Map<String, Map<String, Boolean>> targetAdj) {
        if (targetAdj == null)
            targetAdj = targetsAdjacentOG;

        ArrayList<Target> requiredFor = new ArrayList<>();
        targetAdj.forEach((k, v) -> {
            if (v.getOrDefault(name, false))
                requiredFor.add(allTargets.get(k));
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

    public boolean DoesDependsOn(String tName1, String tName2) {
        return targetsAdjacentOG.get(tName1).get(tName2);
    }

    public boolean DoesRequiredFor(String tName1, String tName2) {
        return targetsAdjacentOG.get(tName2).get(tName1);
    }

    public boolean containsName(String name) {
        return allTargets.containsKey(name);
    }

    public String stringMatrix() {
        StringBuilder res = new StringBuilder("\n  ");
        allTargets.forEach((k, v) -> res.append("   ").append(k));
        res.append('\n');
        targetsAdjacentOG.forEach((k, v) -> {
            res.append(k).append(":");
            allTargets.forEach((name, target) -> {
                if (v.getOrDefault(name, false))
                    res.append(String.format("%4s", 1));
                else
                    res.append(String.format("%4s", 0));
            });
            res.append("\n");
        });
        return res.toString();
    }

//    public String stringMap() {
//        StringBuilder res = new StringBuilder("\n\n");
//        allTargets.forEach((key, value) -> res.append(key).append(": ").append(value).append("\ntype= ").append(getType(key)).append("\n\n"));
//        return res.toString();
//    }

    public Map getStatusesStatistics() {
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
            res.append("    Targets Status: ").append(target.getStatus()).append("\n");
            res.append("        ");
            targetsAdjacentOG.get(target.name).keySet().forEach(k -> {
                res.append(k).append(": ").append(allTargets.get(k).getStatus()).append(" , ");
            });
            res.append("\n");
            res.append("        ");
            targetsAdjacentOG.get(target.name).keySet().forEach(k -> {
                res.append(k).append(": ").append(allTargets.get(k).getResult()).append(" , ");
            });
            res.append("\n");

            res.append("    Process Time: ").append(target.getProcessTime().toString()).append("\n");
            res.append("--------------------\n");
        });
        return res.toString();
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("Number of targets: " + allTargets.size() + '\n');
//        res.append("Types: " + getTypesStatistics());
        return res.toString();
//        return "TargetGraph \n{" +
//                "\nGraphsName='" + GraphsName + '\'' +
//                ",\nWorkingDir='" + WorkingDir + '\'' +
//                ",\ntargetsAdj=" + stringMatrix() +
//                ",\nAllTargets=" + stringMap() +
//                '}';
    }
}
