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
import java.util.List;

public class FindPath extends SideAction {
    private ComboBox<Target> source;
    private ComboBox<Target> dest;
    private boolean choose;

    public FindPath(GraphStage graphStage, Runnable onOpenSettings) {
        super("Find Path", graphStage, onOpenSettings);
        setOnAction(this::chooseTargets);
        choose = true;
        ActionButton findButton = new ActionButton("Find", this::findPath);
        this.settings.getChildren().addAll(new AnchoredNode(findButton), createFindPathHBox(), new HBox(100, new Label("source"), new Label("dest")));
    }

    public HBox createFindPathHBox() {
        source = new ComboBox<>(FXCollections.observableList(new ArrayList<>(graphStage.engine.getAllTargets().values())));
        dest = new ComboBox<>(FXCollections.observableList(new ArrayList<>(graphStage.engine.getAllTargets().values())));

        createComboBoxListener(source, dest);
        createComboBoxListener(dest, source);

        Button opposite = new Button("<-->");
        opposite.setMinWidth(80);
        opposite.setMaxHeight(source.getHeight());
        opposite.setOnAction((ae) -> {
            Target tempS = source.getSelectionModel().getSelectedItem();
            Target tempD = dest.getSelectionModel().getSelectedItem();
            dest.getSelectionModel().clearSelection();
            source.setValue(tempD);
            dest.setValue(tempS);
        });
        HBox findPathHBox = new HBox();
        findPathHBox.setMinWidth(settings.getWidth());
        findPathHBox.getChildren().addAll(source, opposite, dest);
        return findPathHBox;
    }

    private void createComboBoxListener(ComboBox<Target> source, ComboBox<Target> dest) {
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
            if (newValue != dest.getValue()) {
                graphStage.choosingController.manualClick(oldValue);
                graphStage.choosingController.manualClick(newValue);
            } else {
                graphStage.choosingController.manualClick(oldValue);
                source.getSelectionModel().clearSelection();
            }
            choose = true;
        });
    }

    public void onChoose(Target target) {
        System.out.println("target = " + target + " choose " + choose);
        if (choose) {
            choose = false;
            if (source.getValue() == target) {
                source.getSelectionModel().clearSelection();
                return;
            }
            if (dest.getValue() == target) {
                dest.getSelectionModel().clearSelection();
                return;
            }
            if (source.getValue() == null) {
                source.getSelectionModel().select(target);
                return;
            }
            if (dest.getValue() == null) {
                dest.getSelectionModel().select(target);
            }
        }
    }

    private void findPath(ActionEvent actionEvent) {
        if (source.getValue() == null || dest.getValue() == null)
            return;

        LinkedList<List<String>> paths = graphStage.engine.findAllPaths(source.getValue().getName(), dest.getValue().getName());
        paths.forEach(path -> {
            Paint currentColor = Color.color(Math.random(), Math.random(), Math.random());
            Utils.tupleIterator(path, (outbound, inbound) -> {
                System.out.println("the");
                graphStage.graphView.getEdgeLine(graphStage.engine.getAllTargets().get(outbound), graphStage.engine.getAllTargets().get(inbound)).setStroke(currentColor);
            });
        });
        reset();
    }

    private void chooseTargets(ActionEvent actionEvent) {
        onOpenSettings.run();

        graphStage.choosingController.setChoosingState(true, 2);
        graphStage.choosingController.setOnChoose(this::onChoose);
        settings.setVisible(true);
    }

    public void clear() {
        source.getSelectionModel().clearSelection();
        dest.getSelectionModel().clearSelection();
    }

    @Override
    public void reset() {
        super.reset();
        source.getSelectionModel().clearSelection();
        dest.getSelectionModel().clearSelection();
    }
}
