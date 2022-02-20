package app.components;

import TargetGraph.Result;
import TargetGraph.Status;
import TargetGraph.Target;
import graphApp.GraphPane;
import graphApp.actions.SideAction;
import graphApp.actions.task.TaskController;
import graphApp.actions.task.TaskSettings;
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
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class TaskControllerAdmin extends TaskController {
    private final TargetsCheckComboBox<String> targetsComboBox;
    private boolean choose;
    private TextArea taskOutput;
    private boolean paused;

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
        TaskSettings taskSettings = TaskSettings.createTaskSettings(graphPane.graph);
        taskSettings.showAndReturn(graphPane);

        if (!taskSettings.submitted)
            return;

        graphPane.choosingController.setChoosingState(true);
        graphPane.choosingController.setOnChoose(this::onChoose);

        if (!taskSettings.runFromScratch) {
            graphPane.choosingController.chooseTargets(graphPane.graph.getCurrentTargets());
//            taskRun(taskSettings);
            settings.setVisible(true);
            return;
        }

        if (taskSettings.chooseAll)
            graphPane.choosingController.all(null);

//        runButton.setOnAction((ea) -> taskRun(taskSettings));
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
        if (taskSettings.runFromScratch) {
            graphPane.graphView.reset();
        } else {
            targetToRunOn = targetToRunOn.stream().filter(target -> target.getResult() == Result.Failure || target.getStatus() == Status.SKIPPED).collect(Collectors.toSet());
        }
        graphPane.choosingController.clear(null);
        graphPane.choosingController.setChoosingState(false);
        targetsComboBox.setDisable(true);
        graphPane.graphView.hideEdges(targetToRunOn);
        targetToRunOn.forEach(t -> t.init(""));
        graphPane.graph.createNewGraphFromTargetList(targetToRunOn);
        graphPane.setBottom(taskOutput);
    }
//
//    public void taskRun(TaskSettings taskSettings) {
//        Set<Target> targetToRunOn = graphPane.choosingController.getChosenTargets();
//        if (targetToRunOn.size() == 0) {
//            alertWarning("No Targets Chosen");
//            return;
//        }
//
//        BeforeRunning(taskSettings, targetToRunOn);
//        initWorkingThread(taskSettings);
//        initChangingColorThread();
//
//        runButton.setText("Pause");
//        runButton.setOnAction(this::pauseResume);
//    }
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
