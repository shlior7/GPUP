import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    Map<String, Target> AllTargets;

    public TargetGraph() {

    }

    public TargetGraph(String GraphsName, String WorkingDir, List<Target> Targets) throws Exception {
        this.GraphsName = GraphsName;
        this.WorkingDir = WorkingDir;
        AllTargets = new HashMap();
        targetsAdj = new HashMap();
        for (Target t : Targets) {
            this.AllTargets.put(t.name, t);
        }
    }

    public void connect(List<Edge> targetsEdges) {
        for (Edge e : targetsEdges) {
            if (targetsAdj.get(e.out) == null)
                targetsAdj.put(e.out, new HashMap());
            targetsAdj.get(e.out).put(e.in, true);
        }
    }

    public boolean DoesDependsOn(String tName1, String tName2) {
        return targetsAdj.get(tName1).get(tName2);
    }

    public boolean DoesRequiredFor(String tName1, String tName2) {
        return targetsAdj.get(tName2).get(tName1);
    }

    public boolean containsName(String name) {
        return AllTargets.containsKey(name);
    }
//
//    public String stringMatrix() {
//        String res = "";
//        for (int i = 0; i < AllTargets.size(); i++) {
//            for (int j = 0; j < AllTargets.size(); j++) {
//                if (targetsAdj.get(AllTargets.get(i).name).get((AllTargets.get(j).name)) == null)
//                    res.concat(String.format("%10s", 0));
//                else
//                    res.concat(String.format("%10s", 1));
//            }
//            res.concat("");
//        }
//        return res;
//    }

//    public String stringMap() {
//        String res = "";
//        AllTargets.forEach((key, value) -> res.concat(key + ":" + value));
//        return res;
//    }

//    @Override
//    public String toString() {
//        return "TargetGraph{" +
//                "GraphsName='" + GraphsName + '\'' +
//                ", WorkingDir='" + WorkingDir + '\'' +
//                ", targetsAdj=" + stringMatrix() +
//                ", AllTargets=" + stringMap() +
//                '}';
//    }
}
