package graph;

import java.util.Map;
import java.util.Set;

public interface Graph<V> {
    Map<String, Set<V>> getAdjacentMap();

    Map<String, V> getVerticesMap();

    String getVertexInfo(V element);
}
