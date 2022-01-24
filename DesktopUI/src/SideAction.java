import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public abstract class SideAction {
    protected final GraphStage graphStage;
    protected AnchorPane anchoredButton;
    protected Button actionButton;
    protected VBox settings;

    public SideAction(String label, GraphStage graphStage) {
        this.graphStage = graphStage;
        this.actionButton = new Button(label);
        this.anchoredButton = new AnchoredButton(actionButton);
        this.settings = new VBox(10);
        this.settings.setVisible(false);
    }

    public void setOnAction(EventHandler<ActionEvent> eventHandler) {
        actionButton.setOnAction(eventHandler);
    }

    public VBox getSettings() {
        return settings;
    }

    public void setSettings(VBox settings) {
        this.settings = settings;
    }

    public AnchorPane getAnchoredButton() {
        return anchoredButton;
    }

    public void setAnchoredButton(AnchorPane anchoredButton) {
        this.anchoredButton = anchoredButton;
    }

    public Button getActionButton() {
        return actionButton;
    }

    public void setActionButton(Button actionButton) {
        this.actionButton = actionButton;
    }

    public void reset() {
        settings.setVisible(false);
    }
}
