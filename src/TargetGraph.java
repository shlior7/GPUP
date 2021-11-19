import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

class Edge {
    String in, out;

    Edge(String depends, String required) {
        this.in = required;
        this.out = depends;
    }

    Edge(String in, String out, boolean dependsOrRequired) {
        this(dependsOrRequired ? in : out, dependsOrRequired ? out : in);
    }
}

public class TargetGraph {
    String GraphsName;
    String WorkingDir;
    Map<String, Map<String, Boolean>> targetsAdj;
    Map<String, Target> allTargets;

    public TargetGraph() {
    }

    public TargetGraph(String GraphsName, String WorkingDir, List<Target> Targets) throws Exception {
        this.GraphsName = GraphsName;
        this.WorkingDir = WorkingDir;
        allTargets = new HashMap();
        targetsAdj = new HashMap();
        for (Target t : Targets) {
            this.allTargets.put(t.name, t);
            this.targetsAdj.put(t.name, new HashMap());
        }
    }

    public void connect(List<Edge> targetsEdges) {
        for (Edge e : targetsEdges) {
            targetsAdj.get(e.out).put(e.in, true);
            allTargets.get(e.out);
        }
    }

    public String createLogLibrary(String taskName) {
        String currentTime = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").format(LocalDateTime.now());
        String path = taskName + " - " + currentTime;
        CompletableFuture.supplyAsync(() -> new File(path).mkdir()).join();
        return path;
    }

    public void log(String LibraryPath, String targetName, String loggedData) throws IOException {
        File log = new File(LibraryPath + "/" + targetName + ".log");
        log.createNewFile();
        FileWriter myWriter = new FileWriter(log.getPath());
        myWriter.write(loggedData);
        myWriter.close();
    }

    public void runTask(Task task) {
        Logger.libPath = createLogLibrary(task.getName());
        Queue<Target> queue = new LinkedList<Target>();
        allTargets.values().stream().forEach(t -> {
            Type type = getType(t.name);
            if (type == Type.leaf || type == Type.independent)
                queue.add(t);
        });
        ////
        while (!queue.isEmpty()) {
            Target target = queue.poll();
            try {
                target.run(task);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            requiredFor(target.name).forEach(tDad -> { // all dads
                if (tDad.status == Status.FROZEN) {
                    AtomicBoolean AllSonsFinishedSuccessfully = new AtomicBoolean(true);
                    targetsAdj.get(tDad.name).keySet().stream().forEach(tBrotherName -> {
                        Target tBrother = allTargets.get(tBrotherName);
                        if (tBrother.status.DidFailed() || tBrother.status == Status.SKIPPED) {
                            tDad.status = Status.SKIPPED;
                            AllSonsFinishedSuccessfully.set(false);
                        }
                        if (!tBrother.status.isFinished()) {
                            AllSonsFinishedSuccessfully.set(false);
                        }
                    });
                    if (AllSonsFinishedSuccessfully.get())
                        queue.add(tDad);
                }
                ///if all of the sons of t's dad finished succsefully add to queue if any of them failed t's dad is skipped
                //
//                if (targetsAdj.get(tDad.name).keySet().stream().allMatch(tc -> allTargets.get(tc).status.isFinished() && !allTargets.get(tc).status.DidFailed()))
//                    queue.add(tDad);
            });
        }
    }

    public ArrayList<Target> requiredFor(String name) {
        ArrayList<Target> requiredFor = new ArrayList<>();
        targetsAdj.forEach((k, v) -> {
            if (v.getOrDefault(name, false))
                requiredFor.add(allTargets.get(k));
        });
        return requiredFor;
    }

    public Type getType(String name) {
        boolean depends = !targetsAdj.get(name).isEmpty();
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

    public boolean DoesDependsOn(String tName1, String tName2) {
        return targetsAdj.get(tName1).get(tName2);
    }

    public boolean DoesRequiredFor(String tName1, String tName2) {
        return targetsAdj.get(tName2).get(tName1);
    }

    public boolean containsName(String name) {
        return allTargets.containsKey(name);
    }

    public String stringMatrix() {
        StringBuilder res = new StringBuilder("\n  ");
        allTargets.forEach((k, v) -> res.append("   " + k));
        res.append('\n');
        targetsAdj.forEach((k, v) -> {
            res.append(k + ":");
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

    public String stringMap() {
        StringBuilder res = new StringBuilder("\n\n");
        allTargets.forEach((key, value) -> res.append(key + ": " + value + "\ntype= " + getType(key) + "\n\n"));
        return res.toString();
    }

    @Override
    public String toString() {
        return "TargetGraph \n{" +
                "\nGraphsName='" + GraphsName + '\'' +
                ",\nWorkingDir='" + WorkingDir + '\'' +
                ",\ntargetsAdj=" + stringMatrix() +
                ",\nAllTargets=" + stringMap() +
                '}';
    }
}
