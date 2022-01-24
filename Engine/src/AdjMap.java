import java.util.HashMap;
import java.util.Set;

class AdjMap extends HashMap<String, Set<Target>> {
}

class AdjacentMap {
    public AdjMap children;
    public AdjMap parents;

    AdjacentMap() {
        children = new AdjMap();
        parents = new AdjMap();
    }

    public void clone(AdjacentMap adjacentMap) {
        this.children = adjacentMap.children;
        this.parents = adjacentMap.parents;
    }
}
