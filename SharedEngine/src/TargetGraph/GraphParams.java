package TargetGraph;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class GraphParams {
    private Collection<Target> allTargets;
    private String graphsName;
    private String workingDir;
    private Collection<Edge> edges;

    public GraphParams(TargetGraph targetGraph) {
        allTargets = targetGraph.getVerticesMap().values();
        edges = targetGraph.getEdges();
        graphsName = targetGraph.getGraphsName();
        workingDir = targetGraph.getWorkingDir();
    }
}
