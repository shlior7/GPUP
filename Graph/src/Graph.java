import sun.security.provider.certpath.Vertex;

import java.util.Map;
import java.util.Set;

public interface Graph<V> {
    Map<String, Set<V>> getAdjNameMap();

    Map<String, V> getAllElementMap();

    String getVertexInfo(V element);
}
