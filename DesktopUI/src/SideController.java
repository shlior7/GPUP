import javafx.event.ActionEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SideController extends VBox {
    private GraphStage graphStage;
    private List<SideAction> actionList;
    private StackPane settings;

    public SideController(GraphStage graphStage) {
        this.graphStage = graphStage;
        settings = new StackPane();
        actionList = Stream.of(new RunTask(graphStage, this::onOpenSettings), new FindPath(graphStage, this::onOpenSettings), new FindCircuit(graphStage, this::onOpenSettings), new WhatIf(graphStage, this::onOpenSettings)).collect(Collectors.toList());
        this.setSpacing(20);
        this.setPrefWidth(200);
        CreateButtonsVBox();
        createSettingsStack();
    }

    public void CreateButtonsVBox() {
        for (SideAction action : actionList) {
            System.out.println(action.getActionButton().getText());
            this.getChildren().add(action.getAnchoredButton());
        }
        this.getChildren().add(new AnchoredButton("Reset", this::reset));
        this.getChildren().add(settings);
    }

    public void createSettingsStack() {
        actionList.forEach(action -> {
            settings.getChildren().add(action.getSettings());
        });
    }

    public void reset(ActionEvent actionEvent) {
        if (graphStage.engine.isTaskRunning())
            return;
        graphStage.graphView.reset();
        graphStage.engine.reset();
        graphStage.choosingController.setChoosingState(false);
        actionList.forEach((SideAction::reset));
    }

    public void onOpenSettings() {
        reset(null);
    }
}
