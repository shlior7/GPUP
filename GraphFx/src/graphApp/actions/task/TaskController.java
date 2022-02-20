package graphApp.actions.task;

import TargetGraph.Result;
import TargetGraph.Status;
import TargetGraph.Target;
import graphApp.GraphPane;
import graphApp.actions.SideAction;
import graphApp.components.ActionButton;
import graphApp.components.AnchoredNode;
import graphApp.components.TargetsCheckComboBox;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class TaskController extends SideAction {
    protected final ActionButton runButton;
    protected final ProgressBar progressBar;
    protected boolean choose;
    protected TextArea taskOutput;
    protected boolean paused;

    public TaskController(GraphPane graphPane) {
        super("Run Task", graphPane);

        //Both
        runButton = new ActionButton();
        this.progressBar = new ProgressBar();
        this.paused = false;
//        this.progressBar.progressProperty().bind(graphPane.engine.getTaskRunner().getProgress());///server
        settings.getChildren().add(new AnchorPane(progressBar));
        createLogTextArea();
        createColorMap();
    }

    ////BOTH
    public synchronized void changeColors(HashMap<String, AtomicBoolean> flickering) {
        graphPane.graph.getAdjacentMap().keySet().forEach(name -> {
            Target target = graphPane.graph.getVerticesMap().get(name);
            Status status = target.getStatus();
            String stroke = status.getColor();
            String fill = target.getResult().getColor();
            if (status == Status.FINISHED)
                stroke = fill;
            if (status == Status.IN_PROCESS) {
                flickering.putIfAbsent(name, new AtomicBoolean(false));
                stroke = flickering.get(name).get() ? "yellow" : "gold";
                flickering.get(name).set(!flickering.get(name).get());
            }
            graphPane.graphView.getStylableVertex(target).setStyle("-fx-stroke: " + stroke + ";" + "-fx-fill: " + fill + ";");
        });
    }

    public String getInstantTime() {
        taskOutput.setScrollTop(Double.MIN_VALUE);
        taskOutput.deselect();
        return LocalTime.now().toString();
    }


    public void afterRunning() {
        runButton.setText("Finished!");
        paused = false;
//        alertWhenDone();
    }

//
//    protected void initChangingColorThread() {
//        Thread.UncaughtExceptionHandler handler = (th, ex) -> System.out.println("Uncaught exception: " + ex);
//        Thread check = new Thread(() -> {
//            HashMap<String, AtomicBoolean> flickering = new HashMap<>();
//            while (graphPane.engine.isTaskRunning() || graphPane.engine.getTaskRunner() == null) { /// Server
//                changeColors(flickering);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            changeColors(flickering);
//            Platform.runLater(this::afterRunning);
//        }, "ColorChanger");
//        check.setUncaughtExceptionHandler(handler);
//        check.start();
//    }


//    private void alertWhenDone() {
//        Alert information = new Alert(Alert.AlertType.INFORMATION);
//        information.setTitle("Final-Results");
//        information.setContentText(graphStage.engine.getResultStatistics());
//        information.showAndWait();
//    }

    protected void alertWarning(String warning) {
        Alert information = new Alert(Alert.AlertType.WARNING);
        information.setTitle("Warning");
        information.setContentText(warning);
        information.showAndWait();
    }

    public void createColorMap() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL taskUrl = TaskSettings.class.getResource("map.fxml");
        fxmlLoader.setLocation(taskUrl);
        try {
            GridPane map = fxmlLoader.load(taskUrl.openStream());
            map.setMaxWidth(50);
            settings.getChildren().add(map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createLogTextArea() {
        taskOutput = new TextArea();
        AnchorPane.setLeftAnchor(taskOutput, 0.0);
        AnchorPane.setRightAnchor(taskOutput, 0.0);
        AnchorPane.setLeftAnchor(progressBar, 0.0);
        AnchorPane.setRightAnchor(progressBar, 0.0);
        taskOutput.minHeight(100);
        taskOutput.maxHeight(100);
        taskOutput.setStyle("-fx-font-size: 2em;");
//
//        graphPane.engine.getTaskRunner().getTaskOutput().addListener((observable, oldValue, newValue) -> {///server
//            if (!Objects.equals(oldValue, newValue))
//                Platform.runLater(() -> taskOutput.appendText("\n" + getInstantTime() + ".   " + newValue));
//        });
    }
}
