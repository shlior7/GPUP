import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.LinkedList;

public class FindCircuit extends SideAction {
    private ComboBox<Target> targetChooser;
    private boolean choose;

    public FindCircuit(GraphStage graphStage) {
        super("Find Circuit", graphStage);
        setOnAction(this::chooseTargets);
        choose = true;
        createChooserComboBox();
        ActionButton findCircuit = new ActionButton("Find", this::findCircuit);
        this.settings.getChildren().addAll(new AnchoredButton(findCircuit), targetChooser);
    }

    private void findCircuit(ActionEvent actionEvent) {
        if (targetChooser.getValue() == null)
            return;

        LinkedList<String> path = graphStage.engine.findCircuit(targetChooser.getValue().getName());
        Paint currentColor = Color.color(Math.random(), Math.random(), Math.random());
        Utils.tupleIterator(path, (outbound, inbound) -> {
            System.out.println("the");
            graphStage.graphView.getEdgeLine(graphStage.engine.getAllTargets().get(outbound), graphStage.engine.getAllTargets().get(inbound)).setStroke(currentColor);
        });
        reset();
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
                return;
            }
            graphStage.choosingController.manualClick(oldValue);
            graphStage.choosingController.manualClick(newValue);
            choose = true;
        });
    }

    private void chooseTargets(ActionEvent actionEvent) {
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
        }
    }

}
