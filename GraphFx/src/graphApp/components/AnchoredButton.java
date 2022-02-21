package graphApp.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

public class AnchoredButton extends AnchorPane {
    Button button;

    public AnchoredButton(String text, EventHandler<ActionEvent> value) {
        button = new Button(text);
        button.setOnAction(value);
        this.getChildren().add(button);
        AnchorPane.setLeftAnchor(button, 0.0);
        AnchorPane.setRightAnchor(button, 0.0);
    }

    public AnchoredButton(String text) {
        button = new Button(text);
        this.getChildren().add(button);
        AnchorPane.setLeftAnchor(button, 0.0);
        AnchorPane.setRightAnchor(button, 0.0);
    }

    public void setOnAction(EventHandler<ActionEvent> value) {
        button.setOnAction(value);
    }

    public Button getButton() {
        return button;
    }
}
