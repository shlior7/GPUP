import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

public class AnchoredButton extends AnchorPane {

    public AnchoredButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> value) {
        Button task = new Button(text);
        this.getChildren().add(task);
        AnchorPane.setLeftAnchor(task, 0.0);
        AnchorPane.setRightAnchor(task, 0.0);
        task.setOnAction(value);
    }


    public AnchoredButton(Button task) {
        AnchorPane.setLeftAnchor(task, 0.0);
        AnchorPane.setRightAnchor(task, 0.0);
        this.getChildren().add(task);
    }


}
