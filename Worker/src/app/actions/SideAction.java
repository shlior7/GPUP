package app.actions;

import app.GraphStage;
import app.components.AnchoredNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public abstract class SideAction {
    protected final GraphStage graphStage;
    protected Button actionButton;
    protected VBox settings;
    protected Runnable onOpenSettings;

    public SideAction(String label, GraphStage graphStage, Runnable onOpenSettings) {
        this.graphStage = graphStage;
        this.actionButton = new Button(label);
        this.settings = new VBox(10);
        this.settings.setVisible(false);
        this.onOpenSettings = onOpenSettings;
    }

    public void setOnAction(EventHandler<ActionEvent> eventHandler) {
        actionButton.setOnAction(eventHandler);
    }

    public VBox getSettings() {
        return settings;
    }

    public AnchorPane getAnchoredButton() {
        return new AnchoredNode(actionButton);
    }

    public Button getActionButton() {
        return actionButton;
    }

    public void reset() {
        settings.setVisible(false);
        graphStage.choosingController.setChoosingState(false);
    }
}
