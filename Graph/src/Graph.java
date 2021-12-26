import java.util.Map;
import java.util.Set;

public interface Graph<V> {
    Map<String, Set<String>> getAdjNameMap();

    Map<String, V> getAllElementMap();
}
