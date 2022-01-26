package app.components;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

public class AnchoredButton extends AnchorPane {

    public AnchoredButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> value) {
        Button task = new Button(text);
        task.setOnAction(value);
        this.getChildren().add(task);
        AnchorPane.setLeftAnchor(task, 0.0);
        AnchorPane.setRightAnchor(task, 0.0);
    }
}
