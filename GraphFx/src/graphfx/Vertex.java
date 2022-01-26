package graphfx;

public class Vertex<V> {
    private final V element;

    public Vertex(V target) {
        this.element = target;
    }

    public V element() {
        return element;
    }
}
