package graphfx;/*
 * The MIT License
 *
 * Copyright 2019 brunomnsilva@gmail.com.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


import graph.Graph;
import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphPanel<V> extends Pane {

    private final GraphProperties graphProperties;
    private final Graph<V> theGraph;
    private final Map<Vertex<V>, GraphVertexNode<V>> vertexNodes;
    private final Map<V, Vertex<V>> vertices;
    private final Map<String, Vertex<V>> verticesByName;
    private final Set<GraphEdgeLine<V>> edgeNodes;
    private final Map<V, Map<V, GraphEdgeLine<V>>> graphEdgesMap;
    private boolean initialized = false;
    private final boolean edgesWithArrows;

    private final AnimationTimer timer;
    private final double repulsionForce;
    private final double attractionForce;
    private final double attractionScale;

    private static final int AUTOMATIC_LAYOUT_ITERATIONS = 1;
    public BooleanProperty automaticLayoutProperty;

    private final Consumer<V> vertexClickConsumer;
    private PlacementStrategy<V> placementStrategy;

    public GraphPanel(Graph<V> theGraph, GraphProperties properties, Consumer<V> edgeClickConsumer) {
        this(theGraph, properties, edgeClickConsumer, null);
    }

    public GraphPanel(Graph<V> theGraph, GraphProperties properties, Consumer<V> edgeClickConsumer, URI cssFile) {
        if (theGraph == null) {
            throw new IllegalArgumentException("The graph cannot be null.");
        }
        this.theGraph = theGraph;
        this.graphProperties = properties != null ? properties : new GraphProperties();
        this.vertexClickConsumer = edgeClickConsumer;
        this.loadStylesheet(cssFile);
        this.edgesWithArrows = this.graphProperties.getUseEdgeArrow();

        this.repulsionForce = this.graphProperties.getRepulsionForce();
        this.attractionForce = this.graphProperties.getAttractionForce();
        this.attractionScale = this.graphProperties.getAttractionScale();

        vertexNodes = new HashMap<>();
        edgeNodes = new HashSet<>();
        vertices = new HashMap<>();
        verticesByName = new HashMap<>();

        graphEdgesMap = new HashMap<>();

        initNodes();
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                runLayoutIteration();
            }
        };
        this.automaticLayoutProperty = new SimpleBooleanProperty(true);
        this.automaticLayoutProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                timer.start();
            } else {
                timer.stop();
            }
        });

    }

    private synchronized void runLayoutIteration() {
        for (int i = 0; i < AUTOMATIC_LAYOUT_ITERATIONS; i++) {
            resetForces();
            computeForces();
            updateForces();
        }
        applyForces();
    }


    public void init() throws IllegalStateException {
        if (this.getScene() == null) {
            throw new IllegalStateException("You must call this method after the instance was added to a scene.");
        } else if (this.getWidth() == 0 || this.getHeight() == 0) {
            throw new IllegalStateException("The layout for this panel has zero width and/or height");
        } else if (this.initialized) {
            throw new IllegalStateException("Already initialized. Use update() method instead.");
        }

        placementStrategy = new SmartPlacementStrategy<>();
        placementStrategy.place(this.widthProperty().doubleValue(),
                this.heightProperty().doubleValue(),
                this.vertexNodes.values());
        timer.start();
        this.initialized = true;
    }

    private void initNodes() {
        theGraph.getVerticesMap().forEach((name, element) -> {
            Vertex<V> vertex = new Vertex<>(element);
            vertices.put(element, vertex);
            verticesByName.put(name, vertex);
            GraphVertexNode<V> vertexAnchor = new GraphVertexNode<V>(vertex, 0, 0, graphProperties.getVertexRadius(), vertexClickConsumer);
            vertexNodes.put(vertex, vertexAnchor);
            addVertex(vertexAnchor);
            setHoverPane(vertexAnchor);
        });

        theGraph.getVerticesMap().forEach((name, element) -> {
            GraphVertexNode<V> graphVertexOut = vertexNodes.get(vertices.get(element));
            theGraph.getAdjacentMap().get(name).forEach(inboundVertex -> {
                Vertex v = vertices.get(inboundVertex);
                Vertex v2 = verticesByName.get(inboundVertex.toString());
                GraphVertexNode<V> graphVertexIn = vertexNodes.get(v);

                graphVertexIn.addAdjacentVertex(graphVertexOut, true);
                graphVertexOut.addAdjacentVertex(graphVertexIn, false);

                GraphEdgeLine<V> graphEdge = createEdge(graphVertexIn, graphVertexOut);

                graphEdgesMap.putIfAbsent(element, new HashMap<>());
                graphEdgesMap.get(element).putIfAbsent(inboundVertex, graphEdge);

                addEdge(graphEdge);

                if (this.edgesWithArrows) {
                    Arrow arrow = new Arrow(this.graphProperties.getEdgeArrowSize());
                    graphEdge.attachArrow(arrow);
                    this.getChildren().add(arrow);
                }
            });
        });

    }

    private GraphEdgeLine<V> createEdge(GraphVertexNode<V> graphVertexInbound, GraphVertexNode<V> graphVertexOutbound) {
        GraphEdgeLine<V> graphEdge = new GraphEdgeLine<V>(graphVertexInbound, graphVertexOutbound);
        edgeNodes.add(graphEdge);

        return graphEdge;
    }

    private void addVertex(GraphVertexNode<V> v) {
        this.getChildren().add(v);

        String labelText = generateVertexLabel(v.getUnderlyingVertex().element());

        if (graphProperties.getUseVertexTooltip()) {
            Tooltip t = new Tooltip();
            t.setText(v.getUnderlyingVertex().element().toString());
            Tooltip.install(v, t);
        }

        if (graphProperties.getUseVertexLabel()) {
            Label label = new Label(labelText);

            label.addStyleClass("vertex-label");
            this.getChildren().add(label);
            v.attachLabel(label);
        }
    }

    private void addEdge(GraphEdgeLine<V> e) {
        //edges to the back
        this.getChildren().add(0, (Node) e);
        edgeNodes.add(e);
    }

    private String generateVertexLabel(V vertex) {
        return vertex != null ? vertex.toString() : "<NULL>";
    }

    private Bounds getPlotBounds() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE,
                maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        if (vertexNodes.size() == 0) return new BoundingBox(0, 0, getWidth(), getHeight());

        for (GraphVertexNode<V> v : vertexNodes.values()) {
            minX = Math.min(minX, v.getCenterX());
            minY = Math.min(minY, v.getCenterY());
            maxX = Math.max(maxX, v.getCenterX());
            maxY = Math.max(maxY, v.getCenterY());
        }

        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }


    private boolean areAdjacent(GraphVertexNode<V> v, GraphVertexNode<V> u) {
        return v.isAdjacentTo(u);
    }

    public void setVertexPosition(Vertex<V> v, double x, double y) {
        GraphVertexNode<V> node = vertexNodes.get(v);
        if (node != null) {
            node.setPosition(x, y);
        }
    }

    public double getVertexPositionX(Vertex<V> v) {
        GraphVertexNode<V> node = vertexNodes.get(v);
        if (node != null) {
            return node.getPositionCenterX();
        }
        return Double.NaN;
    }

    public double getVertexPositionY(Vertex<V> v) {
        GraphVertexNode<V> node = vertexNodes.get(v);
        if (node != null) {
            return node.getPositionCenterY();
        }
        return Double.NaN;
    }

    public StyledElement getStylableVertex(Vertex<V> v) {
        return vertexNodes.get(v);
    }


    public StyledElement getStylableVertex(V vertexElement) {
        return getGraphVertex(vertexElement);
    }

    public GraphVertexNode<V> getGraphVertex(V vertexElement) {
        return vertexNodes.getOrDefault(vertices.getOrDefault(vertexElement, null), null);
    }

    public StyledElement getStylableLabel(Vertex<V> v) {
        GraphVertexNode<V> vertex = vertexNodes.get(v);

        return vertex != null ? vertex.getStylableLabel() : null;
    }

    private void loadStylesheet(URI cssFile) {
        try {
            String css;
            if (cssFile != null) {
                css = cssFile.toURL().toExternalForm();
            } else {
                css = getClass().getResource("graph.css").toExternalForm();
            }
            getStylesheets().add(css);
            this.getStyleClass().add("graph");
        } catch (MalformedURLException ignored) {
        }
    }


    private double sqr(double x) {
        return x * x;
    }

    private Point2D getClosestPoint(Point2D pt1, Point2D pt2, Point2D p) {
        return getClosestPoint(pt1.getX(), pt1.getY(), pt2.getX(), pt2.getY(), p.getX(), p.getY());
    }

    private Point2D getClosestPoint(double pt1X, double pt1Y, double pt2X, double pt2Y, double pX, double pY) {
        double u = ((pX - pt1X) * (pt2X - pt1X) + (pY - pt1Y) * (pt2Y - pt1Y)) / (sqr(pt2X - pt1X) + sqr(pt2Y - pt1Y));
        if (u > 0.0 && u < 1.0)
            return new Point2D((int) (pt2X * u + pt1X * (1.0 - u) + 0.5), (int) (pt2Y * u + pt1Y * (1.0 - u) + 0.5));
        return null;
    }


    /*
     * AUTOMATIC LAYOUT
     */
    private void computeForces() {
        for (GraphVertexNode<V> v : vertexNodes.values()) {
            for (GraphVertexNode<V> other : vertexNodes.values()) {
                if (v == other) {
                    continue; //NOP
                }

                Point2D repellingForce = UtilitiesPoint2D.repellingForce(v.getUpdatedPosition(), other.getUpdatedPosition(), this.repulsionForce);

                double deltaForceX = 0, deltaForceY = 0;

                if (areAdjacent(v, other)) {

                    Point2D attractiveForce = UtilitiesPoint2D.attractiveForce(v.getUpdatedPosition(), other.getUpdatedPosition(),
                            vertexNodes.size(), this.attractionForce, this.attractionScale);

                    deltaForceX = attractiveForce.getX() + repellingForce.getX();
                    deltaForceY = attractiveForce.getY() + repellingForce.getY();
                } else {
                    deltaForceX = repellingForce.getX();
                    deltaForceY = repellingForce.getY();
                }

                v.addForceVector(deltaForceX, deltaForceY);
            }
        }
    }


    private void updateForces() {
        vertexNodes.values().forEach(GraphVertexNode::updateDelta);
    }

    private void applyForces() {
        vertexNodes.values().forEach(GraphVertexNode::moveFromForces);
    }

    private void resetForces() {
        vertexNodes.values().forEach(GraphVertexNode::resetForces);
    }

    private void setHoverPane(GraphVertexNode<V> vertex) {
        Label text = new Label();
        text.setStyle("-fx-fill: white;-fx-font-size: 25;");
        StackPane stickyNotesPane = new StackPane(text);
        stickyNotesPane.setPrefSize(200, 200);
        stickyNotesPane.setId("tool");
        stickyNotesPane.getStyleClass().add("tooltip");

        Popup popup = new Popup();
        popup.getContent().add(stickyNotesPane);

        vertex.hoverProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                text.setText(theGraph.getVertexInfo(vertex.getUnderlyingVertex().element()));
                Bounds bounds = vertex.localToScreen(vertex.getBoundsInLocal());
                Bounds paneBounds = this.localToScreen(this.getBoundsInLocal());

                boolean passedRight = bounds.getMaxX() + stickyNotesPane.getWidth() >= paneBounds.getMaxX();
                boolean passedBottom = bounds.getMinY() + stickyNotesPane.getHeight() >= paneBounds.getMaxY();

                double sX = passedRight ? bounds.getMinX() - stickyNotesPane.getWidth() - 5 : bounds.getMaxX();
                double sY = passedBottom ? bounds.getMinY() - stickyNotesPane.getHeight() - 5 : bounds.getMaxY();

                popup.show(this, sX, sY);
            } else {
                popup.hide();
            }
        });
    }

    public Property<Boolean> automaticLayoutProperty() {
        return automaticLayoutProperty;
    }

    public void setPressable(boolean pressable) {
        vertexNodes.values().forEach(v -> v.setPressable(pressable));
    }

    public void setPressable(V element, boolean pressable) {
        vertexNodes.get(vertices.get(element)).setPressable(pressable);
    }

    public void pressOnVertex(V element) {
        vertexNodes.get(vertices.get(element)).press();
    }

    public void hideEdges(Set<V> elementsNotIncluded) {
        edgeNodes.forEach((edge) ->
        {
            if (elementsNotIncluded.containsAll(Stream.of(edge.getInbound().getUnderlyingVertex().element(), edge.getOutbound().getUnderlyingVertex().element()).collect(Collectors.toSet())))
                return;
            edge.hide();
        });
    }

    public GraphEdgeLine<V> getEdgeLine(V outbound, V inbound) {
        Map<V, GraphEdgeLine<V>> map = graphEdgesMap.getOrDefault(outbound, null);
        if (map != null) {
            return map.getOrDefault(inbound, null);
        }
        return null;
    }

    public void placeUpsideDown() {
        placementStrategy.reverse(this.widthProperty().doubleValue(),
                this.heightProperty().doubleValue());
    }

    public void reset() {
        edgeNodes.forEach(GraphEdgeLine::show);
        vertexNodes.values().forEach(GraphVertexNode::setVertexStyleToDefault);
        edgeNodes.forEach(GraphEdgeLine::setEdgeStyleToDefault);
    }
}
