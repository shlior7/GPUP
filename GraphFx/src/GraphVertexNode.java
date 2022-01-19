/*
 * The MIT License
 *
 * Copyright 2018 brunomnsilva@gmail.com.
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

import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;


public class GraphVertexNode<T> extends Circle implements StyledElement {

    private final Vertex<T> underlyingVertex;
    private final Set<GraphVertexNode<T>> adjacentVertices;

    private Label attachedLabel = null;

    private final StyleHandler styleProxy;

    private final PointVector forceVector = new PointVector(0, 0);
    private final PointVector updatedPosition = new PointVector(0, 0);


    public GraphVertexNode(Vertex<T> v, double x, double y, double radius, Consumer<T> onClicked) {
        super(x, y, radius);

        this.underlyingVertex = v;
        this.setOnMouseClicked((me) -> onClicked.accept(underlyingVertex.element()));
        this.attachedLabel = null;

        this.adjacentVertices = new HashSet<>();

        styleProxy = new StyleHandler(this);
        styleProxy.addStyleClass("vertex");
    }

    public void addAdjacentVertex(GraphVertexNode<T> v) {
        this.adjacentVertices.add(v);
    }

    public boolean removeAdjacentVertex(GraphVertexNode<T> v) {
        return this.adjacentVertices.remove(v);
    }

    public boolean removeAdjacentVertices(Collection<GraphVertexNode<T>> col) {
        return this.adjacentVertices.removeAll(col);
    }

    public Set<GraphVertexNode<T>> getAdjacents() {
        return this.adjacentVertices;
    }

    public boolean isAdjacentTo(GraphVertexNode<T> v) {
        return this.adjacentVertices.contains(v);
    }

    public Point2D getPosition() {
        return new Point2D(getCenterX(), getCenterY());
    }

    public void setPosition(double x, double y) {
        setCenterX(x);
        setCenterY(y);
    }

    public double getPositionCenterX() {
        return getCenterX();
    }

    public double getPositionCenterY() {
        return getCenterY();
    }


    public void setPosition(Point2D p) {
        setPosition(p.getX(), p.getY());
    }


    public void attachLabel(Label label) {
        this.attachedLabel = label;
        label.xProperty().bind(centerXProperty().subtract(label.getLayoutBounds().getWidth() / 2.0));
        label.yProperty().bind(centerYProperty().add(getRadius() + label.getLayoutBounds().getHeight()));
    }

    public Label getAttachedLabel() {
        return attachedLabel;
    }

    public Vertex<T> getUnderlyingVertex() {
        return underlyingVertex;
    }

    @Override
    public void setStyleClass(String cssClass) {
        styleProxy.setStyleClass(cssClass);
    }

    @Override
    public void addStyleClass(String cssClass) {
        styleProxy.addStyleClass(cssClass);
    }

    @Override
    public boolean removeStyleClass(String cssClass) {
        return styleProxy.removeStyleClass(cssClass);
    }

    public StyledElement getStylableLabel() {
        return this.attachedLabel;
    }


    public void resetForces() {
        forceVector.x = forceVector.y = 0;
        updatedPosition.x = getCenterX();
        updatedPosition.y = getCenterY();
    }


    public void addForceVector(double x, double y) {
        forceVector.x += x;
        forceVector.y += y;
    }


    public Point2D getForceVector() {
        return new Point2D(forceVector.x, forceVector.y);
    }

    public Point2D getUpdatedPosition() {
        return new Point2D(updatedPosition.x, updatedPosition.y);
    }


    public void updateDelta() {
        updatedPosition.x = updatedPosition.x + forceVector.x;
        updatedPosition.y = updatedPosition.y + forceVector.y;
    }

    public void moveFromForces() {

        //limit movement to parent bounds
        double height = getParent().getLayoutBounds().getHeight() - 25;
        double width = getParent().getLayoutBounds().getWidth();

        updatedPosition.x = boundCenterCoordinate(updatedPosition.x, 0, width);
        updatedPosition.y = boundCenterCoordinate(updatedPosition.y, 0, height);

        setPosition(updatedPosition.x, updatedPosition.y);
    }

    private double boundCenterCoordinate(double value, double min, double max) {
        double radius = getRadius();

        if (value < min + radius) {
            return min + radius;
        } else return Math.min(value, max - radius);
    }

    private static class PointVector {
        double x, y;

        public PointVector(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

}
