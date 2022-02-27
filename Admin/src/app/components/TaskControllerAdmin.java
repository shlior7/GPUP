package app.components;

import TargetGraph.Target;
import TargetGraph.TargetGraph;
import com.google.gson.JsonObject;
import graphApp.GraphPane;
import graphApp.actions.task.TaskController;
import graphApp.components.AnchoredNode;
import graphApp.components.TargetsCheckComboBox;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import okhttp3.HttpUrl;
import types.Task;
import utils.Constants;
import utils.Utils;
import utils.http.HttpClientUtil;
import utils.http.SimpleCallBack;

import java.util.*;
import java.util.stream.Collectors;

import static utils.Constants.GSON_INSTANCE;

public class TaskControllerAdmin extends TaskController {
    private final TargetsCheckComboBox<String> targetsComboBox;
    private boolean choose;
    Map<String, TaskSettings> taskSettingsMap;

    public TaskControllerAdmin(GraphPane graphPane) {
        super(graphPane);
        //Admin
        this.targetsComboBox = new TargetsCheckComboBox<>(graphPane.graph.getVerticesMap().values().stream().map(Target::getName).collect(Collectors.toList()), this::onAdd, this::onRemove);
        this.settings.getChildren().addAll(new AnchoredNode(runButton), new AnchoredNode(pauseButton), new AnchoredNode(targetsComboBox));
        taskSettingsMap = new HashMap<>();
        this.choose = true;
        setOnAction(this::chooseTargets);
    }

    public void onAdd(String name) {
        if (!choose) {
            choose = true;
            return;
        }
        choose = false;
        graphPane.choosingController.manualClick(graphPane.graph.getVerticesMap().get(name));
        choose = true;
    }

    public void onRemove(String name) {
        if (!choose) {
            choose = true;
            return;
        }
        choose = false;
        graphPane.choosingController.manualClick(graphPane.graph.getVerticesMap().get(name));
        choose = true;
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
        if (choose) {
            choose = false;
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
        String finalUrl = HttpUrl
                .parse(Constants.TASK_UPLOAD)
                .newBuilder()
                .addQueryParameter(Constants.GRAPHNAME, graphPane.graph.getGraphsName())
                .addQueryParameter(Constants.FROM_SCRATCH, String.valueOf(fromScratch))
                .build()
                .toString();

        System.out.println("finalUrl " + finalUrl);
        JsonObject json = new JsonObject();
        json.addProperty("task", GSON_INSTANCE.toJson(task));
        json.addProperty("targets", GSON_INSTANCE.toJson(targetToRunOn));
        HttpClientUtil.runAsyncBody(finalUrl, String.valueOf(json), new SimpleCallBack());
    }

    public void taskRun(TaskSettings taskSettings) {
        Set<Target> targetToRunOn = graphPane.choosingController.getChosenTargets();
        if (targetToRunOn.size() == 0) {
            Utils.alertWarning("No Targets Chosen");
            return;
        }

        BeforeRunning(taskSettings, targetToRunOn);
        super.start();
    }

    @Override
    public void reset() {
        super.reset();
        paused.set(false);
    }
}
