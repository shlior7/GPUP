/*
 * The MIT License
 *
 * Copyright 2019 brunomnsilva.
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

import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import static javafx.beans.binding.Bindings.createDoubleBinding;

public class GraphEdgeLine<V> extends Line implements StyledElement  {
    private final GraphVertexNode<V> inbound;
    private final GraphVertexNode<V> outbound;
    
    private Label attachedLabel = null;
    private Arrow attachedArrow = null;
    
    /* Styling proxy */
    private final StyleHandler styleProxy;
    
    public GraphEdgeLine(GraphVertexNode<V> inbound, GraphVertexNode<V> outbound) {
        if( inbound == null || outbound == null) {
            throw new IllegalArgumentException("Cannot connect null vertices.");
        }
        
        this.inbound = inbound;
        this.outbound = outbound;
        

        styleProxy = new StyleHandler(this);
        styleProxy.addStyleClass("edge");
        
        //bind start and end positions to vertices centers through properties
        this.startXProperty().bind(outbound.centerXProperty());
        this.startYProperty().bind(outbound.centerYProperty());
        this.endXProperty().bind(inbound.centerXProperty());
        this.endYProperty().bind(inbound.centerYProperty());
    }
    
    public void setStyleClass(String cssClass) {
        styleProxy.setStyleClass(cssClass);
    }

    public void addStyleClass(String cssClass) {
        styleProxy.addStyleClass(cssClass);
    }

    public boolean removeStyleClass(String cssClass) {
        return styleProxy.removeStyleClass(cssClass);
    }
    

    public void attachLabel(Label label) {
        this.attachedLabel = label;
        label.xProperty().bind(startXProperty().add(endXProperty()).divide(2).subtract(label.getLayoutBounds().getWidth() / 2));
        label.yProperty().bind(startYProperty().add(endYProperty()).divide(2).add(label.getLayoutBounds().getHeight() / 1.5));  
    }

    public Label getAttachedLabel() {
        return attachedLabel;
    }

    public void attachArrow(Arrow arrow) {
        this.attachedArrow = arrow;
        
        /* attach arrow to line's endpoint */
        arrow.translateXProperty().bind(endXProperty());
        arrow.translateYProperty().bind(endYProperty());
        
        /* rotate arrow around itself based on this line's angle */
        Rotate rotation = new Rotate();
        rotation.pivotXProperty().bind(translateXProperty());
        rotation.pivotYProperty().bind(translateYProperty());
        rotation.angleProperty().bind( toDegrees(
                atan2( endYProperty().subtract(startYProperty()),
                endXProperty().subtract(startXProperty()))
        ));
        
        arrow.getTransforms().add(rotation);
        
        /* add translation transform to put the arrow touching the circle's bounds */
        Translate t = new Translate(- outbound.getRadius(), 0);
        arrow.getTransforms().add(t);
        
    }

    public Arrow getAttachedArrow() {
        return this.attachedArrow;
    }

    public StyledElement getStylableArrow() {
        return this.attachedArrow;
    }
    
    public StyledElement getStylableLabel() {
        return this.attachedLabel;
    }

    public static DoubleBinding toDegrees(final ObservableDoubleValue angrad) {
        return createDoubleBinding(() -> Math.toDegrees(angrad.get()), angrad);
    }

    public static DoubleBinding atan2(final ObservableDoubleValue y, final ObservableDoubleValue x) {
        return createDoubleBinding(() -> Math.atan2(y.get(), x.get()), y, x);
    }
}
