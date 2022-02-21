package graphApp.actions;


import graphApp.GraphPane;
import graphApp.components.AnchoredButton;
import graphApp.components.AnchoredNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public abstract class SideAction {
    protected final GraphPane graphPane;
    protected AnchoredButton actionButton;
    protected VBox settings;
    protected Runnable onOpenSettings;

    public void setOnOpenSettings(Runnable onOpenSettings) {
        this.onOpenSettings = onOpenSettings;
    }


    public SideAction(String label, GraphPane graphPane) {
        this.graphPane = graphPane;
        this.actionButton = new AnchoredButton(label);
        this.settings = new VBox(10);
        this.settings.setVisible(false);
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

    public AnchoredButton getActionButton() {
        return actionButton;
    }

    public void reset() {
        settings.setVisible(false);
        graphPane.choosingController.setChoosingState(false);
    }
}
