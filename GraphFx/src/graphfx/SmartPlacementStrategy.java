package graphfx;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class SmartPlacementStrategy<V> implements PlacementStrategy<V> {
    List<GraphVertexNode<V>> roots = new LinkedList<>();
    List<GraphVertexNode<V>> leafs = new LinkedList<>();//leafs with independents
    List<GraphVertexNode<V>> middles = new LinkedList<>();

    double precentOfRoots;
    double precentOfLeafs;

    public void place(double width, double height, Collection<? extends GraphVertexNode<V>> vertices) {
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

        precentOfLeafs = (double) leafs.size() / (double) vertices.size();
        precentOfRoots = (double) roots.size() / (double) vertices.size();

        SetMinMaxHeight(0, precentOfRoots, roots);
        SetMinMaxHeight(precentOfRoots, 1 - precentOfLeafs, middles);
        SetMinMaxHeight(1 - precentOfLeafs, 1, leafs);
        Random rand = new Random();


        vertices.forEach(v -> {
            double x = rand.nextDouble() * width;
            double y = (rand.nextDouble() * height);

            v.setPosition(x, y);
        });
    }

    private void SetMinMaxHeight(double minHeightPercent, double maxHeightPercent, List<GraphVertexNode<V>> vertices) {
        for (GraphVertexNode<V> vertex : vertices) {
            vertex.setMinHeightPercent(minHeightPercent);
            vertex.setMaxHeightPercent(maxHeightPercent);
        }
    }

    @Override
    public void reverse(double width, double height) {
        try {
            if (leafs.get(0).getMinHeightPercent() != 0) {
                SetMinMaxHeight(0, precentOfLeafs, leafs);
                SetMinMaxHeight(precentOfLeafs, 1 - precentOfRoots, middles);
                SetMinMaxHeight(1 - precentOfRoots, 1, roots);
            } else {
                SetMinMaxHeight(0, precentOfRoots, roots);
                SetMinMaxHeight(precentOfRoots, 1 - precentOfLeafs, middles);
                SetMinMaxHeight(1 - precentOfLeafs, 1, leafs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
