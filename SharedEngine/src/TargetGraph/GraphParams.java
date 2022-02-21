package TargetGraph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import types.Task;
import types.TaskType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
@AllArgsConstructor
public class GraphParams {
    private Collection<Target> allTargets;
    private String graphsName;
    private String workingDir;
    private Collection<Edge> edges;
    private Map<TaskType, Integer> prices;

    public GraphParams(TargetGraph targetGraph) {
        allTargets = new ArrayList<>();
        edges = new ArrayList<>();
        allTargets = targetGraph.getVerticesMap().values();
        edges = targetGraph.getEdges();
        graphsName = targetGraph.getGraphsName();
        prices = targetGraph.getPrices();
    }
}
