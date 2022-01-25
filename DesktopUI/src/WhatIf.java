import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

public class WhatIf extends SideAction {
    private ComboBox<Target> targetChooser;
    private boolean choose;

    public WhatIf(GraphStage graphStage, Runnable onOpenSettings) {
        super("What If", graphStage, onOpenSettings);
        setOnAction(this::chooseTargets);
        choose = true;
        createChooserComboBox();
        ActionButton findParents = new ActionButton("Find Required For", this::findParents);
        ActionButton findChildren = new ActionButton("Find Depends On", this::findChildren);
        this.settings.getChildren().addAll(new AnchoredButton(findParents), new AnchoredButton(findChildren), targetChooser);
    }

    private void findChildren(ActionEvent actionEvent) {
        if (targetChooser.getValue() == null)
            return;

        Set<Target> parents = graphStage.engine.getTargetGraph().whoAreYourAllBabies(targetChooser.getValue().getName());
        parents.forEach((target) -> {
            graphStage.graphView.getGraphVertex(target).setStroke(Color.CRIMSON);
        });
    }

    private void findParents(ActionEvent actionEvent) {
        if (targetChooser.getValue() == null)
            return;

        Set<Target> parents = graphStage.engine.getTargetGraph().whoAreYourAllDaddies(targetChooser.getValue().getName());
        parents.forEach((target) -> {
            System.out.println("target " + target);
            graphStage.graphView.getGraphVertex(target).setStroke(Color.CRIMSON);
        });
    }

    public void createChooserComboBox() {
        targetChooser = new ComboBox<>(FXCollections.observableList(new ArrayList<>(graphStage.engine.getAllTargets().values())));
        createComboBoxListener(targetChooser);
        targetChooser.setMinWidth(settings.getWidth());
    }


    private void createComboBoxListener(ComboBox<Target> source) {
        source.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if (!choose) {
                choose = true;
                return;
            }
            choose = false;
            System.out.println("old " + oldValue + " new " + newValue);
            if (newValue == null) {
                graphStage.choosingController.manualClick(oldValue);
                choose = true;
                clear();
                return;
            }
            graphStage.choosingController.manualClick(oldValue);
            graphStage.choosingController.manualClick(newValue);
            choose = true;
        });
    }

    private void clear() {
        graphStage.graphView.reset();
    }

    private void chooseTargets(ActionEvent actionEvent) {
        onOpenSettings.run();

        graphStage.choosingController.setChoosingState(true, 1);
        graphStage.choosingController.setOnChoose(this::onChoose);
        settings.setVisible(true);
    }

    public void onChoose(Target target) {
        System.out.println("target = " + target + " choose " + choose);
        if (choose) {
            choose = false;
            if (targetChooser.getValue() == target) {
                targetChooser.getSelectionModel().clearSelection();
                return;
            }
            if (targetChooser.getValue() == null) {
                targetChooser.getSelectionModel().select(target);
            }
            choose = true;
        }
    }

}
