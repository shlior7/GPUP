package graph;

import java.util.Map;
import java.util.Set;

public interface Graph<V> {
    Map<String, Set<V>> getAdjacentNameMap();

    Map<String, V> getAllElementMap();

    String getVertexInfo(V element);
}
