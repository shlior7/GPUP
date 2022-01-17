
import javafx.geometry.Point2D;

import java.util.Collection;
import java.util.Random;

public class SmartPlacementStrategy implements PlacementStrategy {

    int padding = 10;
    int margin = 10;

    @Override
    public <V> void place(double width, double height, Collection<? extends GraphVertexNode<V>> vertices) {
        Point2D center = new Point2D(width / 2, height / 2);
        int N = vertices.size();
        double UpperLine = padding;
        double centerLine = center.getY();
        double bottomLine = height - padding;
        double x = margin;
        for (GraphVertexNode<V> vertex : vertices) {
            x += vertex.getRadius() / 2;
            if (vertex.getAdjacents().size() == 0) {
                vertex.setPosition(x + vertex.getRadius() / 2, bottomLine);
                x += margin + vertex.getRadius();
            }
        }
    }

}
