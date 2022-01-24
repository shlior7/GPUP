
import javafx.geometry.Point2D;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class SmartPlacementStrategy implements PlacementStrategy {

    public <V> void place(double width, double height, Collection<? extends GraphVertexNode<V>> vertices) {
        List<GraphVertexNode<V>> roots = new LinkedList<>();
        List<GraphVertexNode<V>> leafs = new LinkedList<>();//leafs with independents
        List<GraphVertexNode<V>> middles = new LinkedList<>();


        vertices.forEach((vertexNode) -> {
            switch (vertexNode.getType()) {
                case Root:
                    roots.add(vertexNode);
                    break;
                case Middle:
                    middles.add(vertexNode);
                    break;
                default:
                    leafs.add(vertexNode);
            }
        });

        double precentOfLeafs = (double) leafs.size() / (double) vertices.size();
        double precentOfRoots = (double) roots.size() / (double) vertices.size();

        PlaceByType(width, height, 0, precentOfRoots, roots);
        PlaceByType(width, height, precentOfRoots, 1 - precentOfLeafs, middles);
        PlaceByType(width, height, 1 - precentOfLeafs, 1, leafs);
    }

    private <V> void PlaceByType(double width, double height, double minHeightPercent, double maxHeightPercent, List<GraphVertexNode<V>> vertices) {
        Random rand = new Random();
        double minHeight = minHeightPercent * height;
        double maxHeight = maxHeightPercent * height;
        for (GraphVertexNode<V> vertex : vertices) {
            vertex.setMinHeightPercent(minHeightPercent);
            vertex.setMaxHeightPercent(maxHeightPercent);

            double x = rand.nextDouble() * width;
            double y = minHeight + (rand.nextDouble() * maxHeight);

            vertex.setPosition(x, y);
        }
    }
}
