package graphApp.actions;

import TargetGraph.Target;
import graphApp.GraphPane;
import graphApp.components.ActionButton;
import graphApp.components.AnchoredNode;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Set;

public class WhatIf extends SideAction {
    private ComboBox<Target> targetChooser;
    private boolean choose;

    public WhatIf(GraphPane graphPane) {
        super("What If", graphPane);
        setOnAction(this::chooseTargets);
        choose = true;
        createChooserComboBox();
        ActionButton findParents = new ActionButton("Find Required For", this::findParents);
        ActionButton findChildren = new ActionButton("Find Depends On", this::findChildren);
        this.settings.getChildren().addAll(new AnchoredNode(findParents), new AnchoredNode(findChildren), new AnchoredNode(targetChooser));
    }

    private void findChildren(ActionEvent actionEvent) {
        if (targetChooser.getValue() == null)
            return;

        Set<Target> parents = graphPane.graph.whoAreAllYourBabies(targetChooser.getValue().getName());
        parents.forEach((target) -> {
            graphPane.graphView.getGraphVertex(target).setStroke(Color.CRIMSON);
        });
    }

    private void findParents(ActionEvent actionEvent) {
        if (targetChooser.getValue() == null)
            return;

        Set<Target> parents = graphPane.graph.whoAreAllYourDaddies(targetChooser.getValue().getName());
        parents.forEach((target) -> {
            graphPane.graphView.getGraphVertex(target).setStroke(Color.CRIMSON);
        });
    }

    public void createChooserComboBox() {
        targetChooser = new ComboBox<>(FXCollections.observableList(new ArrayList<>(graphPane.graph.getVerticesMap().values())));
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
            if (newValue == null) {
                graphPane.choosingController.manualClick(oldValue);
                choose = true;
                clear();
                return;
            }
            graphPane.choosingController.manualClick(oldValue);
            graphPane.choosingController.manualClick(newValue);
            choose = true;
        });
    }

    private void clear() {
        graphPane.graphView.reset();
    }

    private void chooseTargets(ActionEvent actionEvent) {
        onOpenSettings.run();

        graphPane.choosingController.setChoosingState(true, 1);
        graphPane.choosingController.setOnChoose(this::onChoose);
        settings.setVisible(true);
    }

    public void onChoose(Target target) {
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
