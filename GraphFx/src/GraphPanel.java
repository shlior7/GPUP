/*
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


import javafx.animation.AnimationTimer;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GraphPanel<V> extends Pane {

    private final GraphProperties graphProperties;

    private final Graph<V> theGraph;
    //    private final SmartPlacementStrategy placementStrategy;
    private final Map<Vertex<V>, GraphVertexNode<V>> vertexNodes;
    private final Map<String, Vertex<V>> verteces;
    private final Set<GraphEdgeLine<V>> edgeNodes;

    private boolean initialized = false;
    private final boolean edgesWithArrows;

    private final AnimationTimer timer;
    private final double repulsionForce;
    private final double attractionForce;
    private final double attractionScale;

    private static final int AUTOMATIC_LAYOUT_ITERATIONS = 30;

    private Consumer<GraphEdgeLine> edgeClickConsumer = null;

    public GraphPanel(Graph<V> theGraph, GraphProperties properties) {
        this(theGraph, properties, null);
    }

    public GraphPanel(Graph<V> theGraph, GraphProperties properties, URI cssFile) {

        if (theGraph == null) {
            throw new IllegalArgumentException("The graph cannot be null.");
        }
        this.theGraph = theGraph;
        this.graphProperties = properties != null ? properties : new GraphProperties();
//        this.placementStrategy = placementStrategy != null ? placementStrategy : new SmartRandomPlacementStrategy();
        this.loadStylesheet(cssFile);
        this.edgesWithArrows = this.graphProperties.getUseEdgeArrow();

        this.repulsionForce = this.graphProperties.getRepulsionForce();
        this.attractionForce = this.graphProperties.getAttractionForce();
        this.attractionScale = this.graphProperties.getAttractionScale();

        vertexNodes = new HashMap<>();
        edgeNodes = new HashSet<>();
        verteces = new HashMap<>();

        initNodes();
        timer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                runLayoutIteration();
            }
        };
        
        timer.start();
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

        new RandomPlacementStrategy().place(this.widthProperty().doubleValue(),
                this.heightProperty().doubleValue(),
                this.vertexNodes.values());
//        timer.start();
        this.initialized = true;
    }

    private void initNodes() {
        theGraph.getAllElementMap().forEach((name, element) -> {
            Vertex<V> vertex = new Vertex<>(element);
            verteces.put(name, vertex);
            GraphVertexNode<V> vertexAnchor = new GraphVertexNode<V>(vertex, 0, 0, graphProperties.getVertexRadius());
            vertexNodes.put(vertex, vertexAnchor);
        });

        verteces.forEach((name, vertex) -> {
            theGraph.getAdjNameMap().get(name).forEach(inboundVertex -> {
                GraphVertexNode<V> graphVertexOut = vertexNodes.get(vertex);
                GraphVertexNode<V> graphVertexIn = vertexNodes.get(verteces.get(inboundVertex));
                GraphEdgeLine<V> graphEdge = createEdge(graphVertexIn, graphVertexOut);
                addEdge(graphEdge);

                if (this.edgesWithArrows) {
                    Arrow arrow = new Arrow(this.graphProperties.getEdgeArrowSize());
                    graphEdge.attachArrow(arrow);
                    this.getChildren().add(arrow);
                }
            });
        });

        for (Vertex<V> vertex : vertexNodes.keySet()) {
            GraphVertexNode<V> v = vertexNodes.get(vertex);
            addVertex(v);
        }
    }

    private GraphEdgeLine<V> createEdge(GraphVertexNode<V> graphVertexInbound, GraphVertexNode<V> graphVertexOutbound) {
        GraphEdgeLine<V> graphEdge;
        graphEdge = new GraphEdgeLine<V>(graphVertexInbound, graphVertexOutbound);
        edgeNodes.add(graphEdge);
        return graphEdge;
    }

    private void addVertex(GraphVertexNode<V> v) {
        this.getChildren().add(v);

        String labelText = generateVertexLabel(v.getUnderlyingVertex().element());

        if (graphProperties.getUseVertexTooltip()) {
            Tooltip t = new Tooltip(labelText);
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

    /**
     * Returns the associated stylable element with a graph vertex.
     *
     * @param vertexElement underlying vertex's element
     * @return stylable element
     */
    public StyledElement getStylableVertex(V vertexElement) {
        for (Vertex<V> v : vertexNodes.keySet()) {
            if (v.element().equals(vertexElement)) {
                return vertexNodes.get(v);
            }
        }
        return null;
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
                File f = new File("./GraphFx/graph.css");
                css = f.toURI().toURL().toExternalForm();
            }

            getStylesheets().add(css);
            this.getStyleClass().add("graph");
        } catch (MalformedURLException ex) {
            Logger.getLogger(GraphPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
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

                //double k = Math.sqrt(getWidth() * getHeight() / graphVertexMap.size());
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

}
