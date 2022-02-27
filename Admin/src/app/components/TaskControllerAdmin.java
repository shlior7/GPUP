package app.components;

import TargetGraph.Target;
import com.google.gson.JsonObject;
import graphApp.GraphPane;
import graphApp.actions.task.TaskController;
import graphApp.components.AnchoredNode;
import graphApp.components.TargetsCheckComboBox;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import types.Task;
import utils.Constants;
import utils.http.HttpClientUtil;
import utils.http.SimpleCallBack;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static utils.Constants.GSON_INSTANCE;

public class TaskControllerAdmin extends TaskController {
    private final TargetsCheckComboBox<String> targetsComboBox;
    private boolean choose;
    private TextArea taskOutput;
    private boolean paused;
    TaskSettings taskSettings;

    public TaskControllerAdmin(GraphPane graphPane) {
        super(graphPane);
        //Admin
        this.targetsComboBox = new TargetsCheckComboBox<>(graphPane.graph.getVerticesMap().values().stream().map(Target::getName).collect(Collectors.toList()), this::onAdd, this::onRemove);
        this.settings.getChildren().addAll(new AnchoredNode(runButton), new AnchoredNode(targetsComboBox));
        this.choose = true;
        setOnAction(this::chooseTargets);
    }


    ///Admin
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

    void chooseTargets(ActionEvent event) {
        if (graphPane.choosingController.isChoosing() || paused)
            return;
        if (taskSettings == null)
            taskSettings = TaskSettings.createTaskSettings(graphPane.graph, null);
        taskSettings.showAndReturn();

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
        
        if (taskSettings.chooseAll)
            graphPane.choosingController.all(null);

        runButton.setOnAction((ea) -> taskRun(taskSettings));
        runButton.setText("Start");
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
        graphPane.choosingController.clear(null);
        graphPane.choosingController.setChoosingState(false);
        targetsComboBox.setDisable(true);
        graphPane.graphView.hideEdges(targetToRunOn);
        graphPane.setBottom(taskOutput);
        uploadTask(TaskSettings.Task, targetToRunOn, taskSettings.runFromScratch);
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
            alertWarning("No Targets Chosen");
            return;
        }

        BeforeRunning(taskSettings, targetToRunOn);

        runButton.setText("Pause");
        super.start(TaskSettings.getTaskName());
//        runButton.setOnAction(this::pauseResume);
    }

    //
//    ///Admin
//    public void initWorkingThread(TaskSettings taskSettings) {
//        Thread.UncaughtExceptionHandler handler = (th, ex) -> System.out.println("Uncaught exception: " + ex);
//        Thread work = new Thread(() -> {
//            if (taskSettings.runFromScratch) {
//                graphPane.engine.runTask(taskSettings.task, taskSettings.maxThreads); // server
//            } else {
//                graphPane.engine.runTaskIncrementally(); // server
//            }
//        }, "TaskRunner");
//        work.setUncaughtExceptionHandler(handler);
//        work.start();
//    }
//
//    private void pauseResume(ActionEvent actionEvent) {
//        if (!graphPane.engine.isTaskRunning()) {//Server
//            return;
//        }
//        if (graphPane.engine.toggleTaskRunning()) {
//            runButton.setText("Resume");
//            paused = true;
//        } else {
//            runButton.setText("Pause");
//            paused = false;
//        }
//    }

    @Override
    public void reset() {
        super.reset();
        paused = false;
    }
}
