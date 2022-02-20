package TargetGraph;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@AllArgsConstructor
public class GraphParams {
    private Collection<Target> allTargets;
    private String graphsName;
    private String workingDir;
    private Collection<Edge> edges;

    public GraphParams(TargetGraph targetGraph) {
        allTargets = new ArrayList<>();
        edges = new ArrayList<>();
        allTargets = targetGraph.getVerticesMap().values();
        edges = targetGraph.getEdges();
        graphsName = targetGraph.getGraphsName();
        workingDir = targetGraph.getWorkingDir();
    }
}
