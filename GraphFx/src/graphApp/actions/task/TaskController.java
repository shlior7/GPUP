package graphApp.actions.task;

import TargetGraph.Status;
import TargetGraph.Target;
import com.google.gson.JsonObject;
import graphApp.GraphPane;
import graphApp.actions.SideAction;
import graphApp.components.ActionButton;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import types.TaskStatus;
import utils.Constants;
import utils.Utils;
import utils.http.HttpClientUtil;
import utils.http.SimpleCallBack;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static utils.Constants.GSON_INSTANCE;

public class TaskController extends SideAction {
    protected final ActionButton runButton;
    protected final ProgressBar progressBar;
    protected TextArea taskOutput;
    protected AtomicBoolean paused;
    protected AtomicBoolean isTaskRunning;
    protected Timer updateTimer;
    Object threadLock = new Object();
    private Timer changingColorTimer;

    public TaskController(GraphPane graphPane) {
        super("Run Task", graphPane);
        //Both
        runButton = new ActionButton();
        this.progressBar = new ProgressBar();
        this.paused = new AtomicBoolean(false);
        this.isTaskRunning = new AtomicBoolean(false);
        settings.getChildren().add(new AnchorPane(progressBar));
        createLogTextArea();
        createColorMap();
    }


    public String getInstantTime() {
        taskOutput.setScrollTop(Double.MIN_VALUE);
        taskOutput.deselect();
        return LocalTime.now().toString();
    }


    public void afterRunning() {
        runButton.setText("Finished!");
        paused.set(false);
//        alertWhenDone();
    }

    public void start(String taskName) {
        isTaskRunning.set(true);
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            public void run() {
                getUpdate(taskName);
            }
        }, 0, 1000);

        changingColorTimer = new Timer();
        changingColorTimer.schedule(new TimerTask() {
            final HashMap<String, AtomicBoolean> flickering = new HashMap<>();

            public void run() {
                changeTargetsColors(flickering);
            }
        }, 0, 1000);
    }


    private void getUpdate(String taskName) {
        if (!isTaskRunning.get())
            return;
        try {

            String url = HttpClientUtil.createUrl(Constants.UPDATE_PROGRESS_GET_URL, Utils.tuple(Constants.TASKNAME, taskName));
            System.out.println(url);

            HttpClientUtil.runAsync(url, new SimpleCallBack((updateJson) -> {
                System.out.println("updateJson = " + updateJson);
                JsonObject json = GSON_INSTANCE.fromJson(updateJson, JsonObject.class);

                String targetsString = json.get("targets").toString().replaceAll("\\s", "");
                Target[] targets = GSON_INSTANCE.fromJson(targetsString, Target[].class);

                String progressString = json.get("progress").toString().replaceAll("\\s", "");
                Double progress = GSON_INSTANCE.fromJson(progressString, Double.class);

                String taskStatusString = json.get("taskStatus").toString().replaceAll("\\s", "");
                TaskStatus taskStatus = GSON_INSTANCE.fromJson(taskStatusString, TaskStatus.class);

                graphPane.graph.updateAllTarget(targets);
                progressBar.progressProperty().set(progress);
                switch (taskStatus) {
                    case PAUSED:
                        paused.set(true);
                        break;
                    case ACTIVE:
                        paused.set(false);
                        resume();
                        break;
                    case STOPPED:
                    case FINISHED:
                        paused.set(false);
                        isTaskRunning.set(false);
                        resume();
                }

            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        if (paused.get())
            synchronized (graphPane.graph) {
                threadLock.notifyAll();
            }
    }

    protected void changeTargetsColors(HashMap<String, AtomicBoolean> flickering) {
        if (!isTaskRunning.get()) {
            changeColors(flickering);
            Platform.runLater(this::afterRunning);
            changingColorTimer.cancel();
        }
        System.out.println("changing colors " + paused.get());
        if (paused.get() && graphPane.graph.getStatusesStatistics().get(Status.IN_PROCESS).size() == 0) {
            try {
                synchronized (graphPane.graph) {
                    threadLock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        changeColors(flickering);
    }


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
        URL taskUrl = TaskController.class.getResource("map.fxml");
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
}
