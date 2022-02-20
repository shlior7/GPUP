package graphApp.components;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

public class AnchoredNode extends AnchorPane {
    public AnchoredNode(Node node) {
        AnchorPane.setLeftAnchor(node, 0.0);
        AnchorPane.setRightAnchor(node, 0.0);
        this.getChildren().add(node);
    }
}
