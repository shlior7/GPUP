
import java.util.Collection;
import java.util.Random;

public class RandomPlacementStrategy implements PlacementStrategy {

    @Override
    public <V> void place(double width, double height, Collection<? extends GraphVertexNode<V>> vertices) {

        Random rand = new Random();

        for (GraphVertexNode<V> vertex : vertices) {

            double x = rand.nextDouble() * width;
            double y = rand.nextDouble() * height;

            vertex.setPosition(x, y);

        }
    }
}
