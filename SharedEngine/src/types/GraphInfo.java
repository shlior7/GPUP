package types;

import TargetGraph.TargetGraph;
import TargetGraph.Type;
import lombok.ToString;

import java.util.Map;

@ToString
public class GraphInfo {
    private final String graphName;
    private final String createdBy;
    private final String totalTargets;
    private final String leaves;
    private final String middles;
    private final String independents;
    private final String roots;

    public GraphInfo(TargetGraph graph) {
        this.graphName = (graph.getGraphsName());
        this.createdBy = graph.getCreatedBy().getName();
        this.totalTargets = String.valueOf(graph.totalSize());
        Map<Type, Integer> typeIntegerMap = graph.getTypesStatistics();
        this.leaves = String.valueOf(typeIntegerMap.get(Type.leaf));
        this.independents = String.valueOf(typeIntegerMap.get(Type.independent));
        this.middles = String.valueOf(typeIntegerMap.get(Type.middle));
        this.roots = String.valueOf(typeIntegerMap.get(Type.root));
    }

    public String getGraphName() {
        return this.graphName;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public String getTotalTargets() {
        return this.totalTargets;
    }

    public String getLeaves() {
        return this.leaves;
    }

    public String getMiddles() {
        return this.middles;
    }

    public String getIndependents() {
        return this.independents;
    }

    public String getRoots() {
        return this.roots;
    }
}
