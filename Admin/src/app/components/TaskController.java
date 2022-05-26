package app.components;

import TargetGraph.Status;
import TargetGraph.Target;
import TargetGraph.TargetGraph;
import com.google.gson.JsonObject;
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
import javafx.scene.layout.Pane;
import okhttp3.HttpUrl;
import types.Task;
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
import java.util.stream.Collectors;

import static utils.Constants.GSON_INSTANCE;

public class TaskController extends SideAction {
    protected final ActionButton runButton;
    protected final ActionButton pauseButton;
    protected final ProgressBar progressBar;
    protected TextArea taskOutput;
    protected AtomicBoolean paused;
    protected AtomicBoolean isTaskRunning;
    protected Timer updateTimer;
    Object threadLock = new Object();
    private Timer changingColorTimer;
    protected String taskName;
    private final TargetsCheckComboBox<String> targetsComboBox;
    private boolean chooseFlag;

    public TaskController(GraphPane graphPane) {
        super("Run Task", graphPane);
        this.runButton = new ActionButton("Start");
        this.pauseButton = new ActionButton("Pause", this::pauseResume);
        this.progressBar = new ProgressBar();
        this.paused = new AtomicBoolean(false);
        this.isTaskRunning = new AtomicBoolean(false);
        pauseButton.setVisible(false);
        createLogTextArea();
        createColorMap();
        this.targetsComboBox = new TargetsCheckComboBox<>(graphPane.graph.getVerticesMap().values().stream().map(Target::getName).collect(Collectors.toList()), this::onAdd, this::onRemove);
        this.settings.getChildren().addAll(createColorMap(), new AnchorPane(progressBar), new AnchoredNode(runButton), new AnchoredNode(pauseButton), new AnchoredNode(targetsComboBox));
        this.chooseFlag = true;
        setOnAction(this::chooseTargets);
    }


    public String getInstantTime() {
        taskOutput.setScrollTop(Double.MIN_VALUE);
        taskOutput.deselect();
        return LocalTime.now().toString();
    }


    public void afterRunning() {
        pauseButton.setVisible(false);
        paused.set(false);
        isTaskRunning.set(false);
        updateTimer.cancel();
        changingColorTimer.cancel();
        alertWhenDone();
    }

    public void start() {
        isTaskRunning.set(true);
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            public void run() {
                getUpdate(taskName);
            }
        }, 2000, 2000);

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

            HttpClientUtil.runAsync(url, new SimpleCallBack((updateJson) -> {
                JsonObject json = GSON_INSTANCE.fromJson(updateJson, JsonObject.class);
                if (json == null)
                    return;

                String targetsString = json.get("targets").toString().replaceAll("\\s", "");
                Target[] targets = GSON_INSTANCE.fromJson(targetsString, Target[].class);

                String progressString = json.get("progress").toString().replaceAll("\\s", "");
                Double progress = GSON_INSTANCE.fromJson(progressString, Double.class);

                String taskStatusString = json.get("taskStatus").toString().replaceAll("\\s", "");
                TaskStatus taskStatus = GSON_INSTANCE.fromJson(taskStatusString, TaskStatus.class);

                String targetLogsString = json.get("targetLogs").toString();
                String[] targetLogs = GSON_INSTANCE.fromJson(targetLogsString, String[].class);

                graphPane.graph.updateAllTarget(targets);
                progressBar.progressProperty().set(progress);
                Platform.runLater(() -> taskOutput.setText(String.join("\n", targetLogs)));

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
                        getSingleUpdateFromServer(taskName);
                        Platform.runLater(this::afterRunning);
                        resume();
                        break;
                }

            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getSingleUpdateFromServer(String taskName) {
        String url = HttpClientUtil.createUrl(Constants.UPDATE_PROGRESS_GET_URL, Utils.tuple(Constants.TASKNAME, taskName));

        HttpClientUtil.runAsync(url, new SimpleCallBack((updateJson) -> {
            JsonObject json = GSON_INSTANCE.fromJson(updateJson, JsonObject.class);
            if (json == null)
                return;

            String targetsString = json.get("targets").toString().replaceAll("\\s", "");
            Target[] targets = GSON_INSTANCE.fromJson(targetsString, Target[].class);

            String progressString = json.get("progress").toString().replaceAll("\\s", "");
            Double progress = GSON_INSTANCE.fromJson(progressString, Double.class);

            graphPane.graph.updateAllTarget(targets);
            progressBar.progressProperty().set(progress);
        }));
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
            changingColorTimer.cancel();
        }
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


    private void alertWhenDone() {
        Alert information = new Alert(Alert.AlertType.INFORMATION);
        information.setTitle("Final Results");
        information.setContentText(graphPane.graph.getStringResultStatistics());
        information.showAndWait();
    }

    public GridPane createColorMap() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL taskUrl = TaskController.class.getResource("map.fxml");
        fxmlLoader.setLocation(taskUrl);
        try {
            GridPane map = fxmlLoader.load(taskUrl.openStream());
            map.setMaxWidth(50);
            return map;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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


    protected void pauseResume(ActionEvent actionEvent) {
        if (!isTaskRunning.get()) {
            return;
        }

        SimpleCallBack callBack = new SimpleCallBack((s) -> {
            if (!paused.get()) {
                runButton.setText("Resume");
                paused.set(true);
            } else {
                runButton.setText("Pause");
                paused.set(false);
            }
        });

        if (!paused.get()) {
            HttpClientUtil.runAsync(HttpClientUtil.createUrl(Constants.TASK_PAUSE_URL, Utils.tuple(Constants.TASKNAME, taskName)), callBack);
        } else {
            HttpClientUtil.runAsync(HttpClientUtil.createUrl(Constants.TASK_RESUME_URL, Utils.tuple(Constants.TASKNAME, taskName)), callBack);
        }

    }


    public void onAdd(String name) {
        if (!chooseFlag) {
            chooseFlag = true;
            return;
        }
        chooseFlag = false;
        graphPane.choosingController.manualClick(graphPane.graph.getVerticesMap().get(name));
        chooseFlag = true;
    }

    public void onRemove(String name) {
        if (!chooseFlag) {
            chooseFlag = true;
            return;
        }
        chooseFlag = false;
        graphPane.choosingController.manualClick(graphPane.graph.getVerticesMap().get(name));
        chooseFlag = true;
    }

    public TaskSettings openTaskSettings(TargetGraph tasksGraph) {
        TaskSettings taskSettings = null;
        try {
            FXMLLoader loader = new FXMLLoader();
            Pane root = loader.load(getClass().getResource("taskSettings.fxml").openStream());
            taskSettings = loader.getController();
            taskSettings.init(tasksGraph, tasksGraph.getPrices().keySet());
            taskSettings.showAndReturn(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return taskSettings;
    }

    void chooseTargets(ActionEvent event) {
        if (graphPane.choosingController.isChoosing() || paused.get())
            return;

        TaskSettings taskSettings = openTaskSettings(graphPane.graph);

        if (taskSettings == null)
            return;

        if (!taskSettings.submitted)
            return;

        graphPane.choosingController.setChoosingState(true);
        graphPane.choosingController.setOnChoose(this::onChoose);


        if (taskSettings.runningNumber > 0) {
            graphPane.choosingController.chooseTargets(graphPane.graph.getCurrentTargets());
            taskRun(taskSettings);
            settings.setVisible(true);
            return;
        }

        Platform.runLater(() -> runButton.setText("Start"));

        if (taskSettings.chooseAll) {
            graphPane.choosingController.all(null);
        }

        runButton.setOnAction((ea) -> taskRun(taskSettings));
        settings.setVisible(true);
        targetsComboBox.setDisable(false);
    }

    public void onChoose(Target target) {
        if (chooseFlag) {
            chooseFlag = false;
            targetsComboBox.getCheckModel().toggleCheckState(target.name);
        }
    }

    private void BeforeRunning(TaskSettings taskSettings, Set<Target> targetToRunOn) {
        if (taskSettings.runningNumber > 0) {
            if (taskSettings.runFromScratch) {
                graphPane.graphView.reset();
                targetToRunOn.forEach(t -> t.init(""));
            }
        } else {
            graphPane.graph.createNewGraphFromTargetList(targetToRunOn);
        }
        Platform.runLater(() -> {
            runButton.setText("Stop");
            runButton.setOnAction(this::stop);
            pauseButton.setVisible(true);
        });

        isTaskRunning.set(true);
        graphPane.choosingController.clear(null);
        graphPane.choosingController.setChoosingState(false);
        targetsComboBox.setDisable(true);
        graphPane.graphView.hideEdges(targetToRunOn);
        graphPane.setBottom(taskOutput);

        uploadTask(taskSettings.Task, targetToRunOn, taskSettings.runFromScratch);

        this.taskName = taskSettings.getTaskName();

        graphPane.setBottom(taskOutput);
    }

    private void stop(ActionEvent actionEvent) {
        String url = HttpClientUtil.createUrl(Constants.TASK_STOP_URL, Utils.tuple(Constants.TASKNAME, taskName));
        HttpClientUtil.runAsync(url, new SimpleCallBack());
    }

    public void uploadTask(Task task, Set<Target> targetToRunOn, boolean fromScratch) {
        String url = HttpClientUtil.createUrl(Constants.TASK_UPLOAD,
                Utils.tuple(Constants.GRAPHNAME, graphPane.graph.getGraphsName()),
                Utils.tuple(Constants.FROM_SCRATCH, String.valueOf(fromScratch)));

        JsonObject json = new JsonObject();
        json.addProperty("task", GSON_INSTANCE.toJson(task));
        json.addProperty("targets", GSON_INSTANCE.toJson(targetToRunOn));
        HttpClientUtil.runAsyncBody(url, String.valueOf(json), new SimpleCallBack());
    }

    public void taskRun(TaskSettings taskSettings) {
        Set<Target> targetToRunOn = graphPane.choosingController.getChosenTargets();
        if (targetToRunOn.size() == 0) {
            Utils.alertWarning("No Targets Chosen");
            return;
        }

        BeforeRunning(taskSettings, targetToRunOn);
        start();
    }

    @Override
    public void reset() {
        super.reset();
        paused.set(false);
    }

}
