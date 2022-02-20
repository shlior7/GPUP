package graphApp.actions;

import TargetGraph.Target;
import graphApp.GraphPane;
import graphApp.components.ActionButton;
import graphApp.components.AnchoredNode;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import utils.Utils;

import java.util.ArrayList;
import java.util.LinkedList;

public class FindCircuit extends SideAction {
    private ComboBox<Target> targetChooser;
    private boolean choose;

    public FindCircuit(GraphPane graphPane) {
        super("Find Circuit", graphPane);
        setOnAction(this::chooseTargets);
        choose = true;
        createChooserComboBox();
        ActionButton findCircuit = new ActionButton("Find", this::findCircuit);
        this.settings.getChildren().addAll(new AnchoredNode(findCircuit), targetChooser);
    }

    private void findCircuit(ActionEvent actionEvent) {
        if (targetChooser.getValue() == null)
            return;

        LinkedList<String> path = graphPane.graph.findCircuit(targetChooser.getValue().getName());
        Paint currentColor = Color.color(Math.random(), Math.random(), Math.random());
        Utils.tupleIterator(path, (outbound, inbound) -> {
            graphPane.graphView.getEdgeLine(graphPane.graph.getVerticesMap().get(outbound), graphPane.graph.getVerticesMap().get(inbound)).setStroke(currentColor);
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
                return;
            }
            graphPane.choosingController.manualClick(oldValue);
            clear();
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
        System.out.println("choose target to change in comboBox = " + target);
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
