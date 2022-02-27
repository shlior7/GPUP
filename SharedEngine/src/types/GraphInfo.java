package types;

import TargetGraph.TargetGraph;
import TargetGraph.Type;
import lombok.ToString;

import java.util.Map;

import static utils.Utils.getStringValueOrZero;
import static utils.Utils.ifNullZero;

@ToString
public class GraphInfo extends TableItem {
    private final String graphName;
    private final String createdBy;
    private final String totalTargets;
    private final String leaves;
    private final String middles;
    private final String independents;
    private final String roots;

    public GraphInfo(TargetGraph graph) {
        super(graph.getGraphsName());
        this.graphName = graph.getGraphsName();
        this.createdBy = graph.getCreatedBy().getName();
        this.totalTargets = String.valueOf(graph.totalSize());
        
        Map<Type, Integer> typeIntegerMap = graph.getTypesStatistics();
        this.leaves = getStringValueOrZero(String.valueOf(typeIntegerMap.get(Type.leaf)));
        this.independents = getStringValueOrZero(typeIntegerMap.get(Type.independent));
        this.middles = getStringValueOrZero(typeIntegerMap.get(Type.middle));
        this.roots = getStringValueOrZero(typeIntegerMap.get(Type.root));
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
