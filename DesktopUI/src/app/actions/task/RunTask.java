package app.actions.task;

import TargetGraph.Result;
import TargetGraph.Status;
import TargetGraph.Target;
import app.GraphStage;
import app.actions.SideAction;
import app.components.ActionButton;
import app.components.AnchoredNode;
import app.components.TargetsCheckComboBox;
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

public class RunTask extends SideAction {
    private final TargetsCheckComboBox<String> targetsComboBox;
    private final ActionButton runButton;
    private final ProgressBar progressBar;
    private boolean choose;
    private TextArea taskOutput;
    private boolean paused;

    public RunTask(GraphStage graphStage, Runnable onOpenSettings) {
        super("Run Task", graphStage, onOpenSettings);
        this.progressBar = new ProgressBar();
        this.runButton = new ActionButton();
        this.targetsComboBox = new TargetsCheckComboBox<>(graphStage.engine.getAllTargets().values().stream().map(Target::getName).collect(Collectors.toList()), this::onAdd, this::onRemove);
        this.settings.getChildren().addAll(new AnchoredNode(runButton), new AnchoredNode(targetsComboBox));
        this.choose = true;
        this.paused = false;
        createLogTextArea();
        createColorMap();
        setOnAction(this::chooseTargets);
        progressBar.progressProperty().bind(graphStage.engine.getTaskRunner().getProgress());
        settings.getChildren().add(new AnchorPane(progressBar));
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

        graphStage.engine.getTaskRunner().getTaskOutput().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(oldValue, newValue))
                Platform.runLater(() -> taskOutput.appendText("\n" + getInstantTime() + ".   " + newValue));
        });
    }


    public void onAdd(String name) {
        if (!choose) {
            choose = true;
            return;
        }
        choose = false;
        graphStage.choosingController.manualClick(graphStage.engine.getAllTargets().get(name));
        choose = true;
    }

    public void onRemove(String name) {
        if (!choose) {
            choose = true;
            return;
        }
        choose = false;
        graphStage.choosingController.manualClick(graphStage.engine.getAllTargets().get(name));
        choose = true;
    }

    void chooseTargets(ActionEvent event) {
        if (graphStage.choosingController.isChoosing() || paused)
            return;
        TaskSettings taskSettings = TaskSettings.createTaskSettings(graphStage.engine.didTaskAlreadyRan());
        taskSettings.showAndReturn(graphStage.engine.getMaxThreads(), graphStage);

        if (!taskSettings.submitted)
            return;

        graphStage.choosingController.setChoosingState(true);
        graphStage.choosingController.setOnChoose(this::onChoose);

        if (!taskSettings.runFromScratch) {
            graphStage.choosingController.chooseTargets(graphStage.engine.getTargetGraph().getCurrentTargets());
            taskRun(taskSettings);
            settings.setVisible(true);
            return;
        }

        if (taskSettings.chooseAll)
            graphStage.choosingController.all(null);

        runButton.setOnAction((ea) -> taskRun(taskSettings));
        runButton.setText("Start");
        settings.setVisible(true);
    }

    public void onChoose(Target target) {
        if (choose) {
            choose = false;
            targetsComboBox.getCheckModel().toggleCheckState(target.name);
        }
    }

    public synchronized void changeColors(HashMap<String, AtomicBoolean> flickering) {
        graphStage.engine.getAdjacentMap().keySet().forEach(name -> {
            Target target = graphStage.engine.getAllTargets().get(name);
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
            graphStage.graphView.getStylableVertex(target).setStyle("-fx-stroke: " + stroke + ";" + "-fx-fill: " + fill + ";");
        });
    }

    public String getInstantTime() {
        taskOutput.setScrollTop(Double.MIN_VALUE);
        taskOutput.deselect();
        return LocalTime.now().toString();
    }

    public void taskRun(TaskSettings taskSettings) {
        Set<Target> targetToRunOn = graphStage.choosingController.getChosenTargets();
        if (targetToRunOn.size() == 0) {
            alertWarning("No Targets Chosen");
            return;
        }

        BeforeRunning(taskSettings, targetToRunOn);
        initWorkingThread(taskSettings);
        initChangingColorThread();

        runButton.setText("Pause");
        runButton.setOnAction(this::pauseResume);
    }

    private void BeforeRunning(TaskSettings taskSettings, Set<Target> targetToRunOn) {
//        boolean taskAlreadyRan = graphStage.engine.didTaskAlreadyRan();
//        boolean choseTargetThatDidntRunYet = targetToRunOn.stream().anyMatch(t -> t.getStatus() == Status.FROZEN);
//
//        if (!taskSettings.runFromScratch && (choseTargetThatDidntRunYet || !taskAlreadyRan)) {
//            alertWarning(choseTargetThatDidntRunYet ? "You can only choose targets that already ran \n Running from scratch on the targets you chose..." : "There were no task that ran yet\n Running from scratch on the targets you chose...");
//            taskSettings.runFromScratch = true;
//        }

        if (taskSettings.runFromScratch) {
            graphStage.graphView.reset();
        } else {
            targetToRunOn = targetToRunOn.stream().filter(target -> target.getResult() == Result.Failure || target.getStatus() == Status.SKIPPED).collect(Collectors.toSet());
        }

        graphStage.choosingController.clear(null);
        graphStage.choosingController.setChoosingState(false);

        graphStage.graphView.hideEdges(targetToRunOn);
        targetToRunOn.forEach(t -> t.init(""));
        graphStage.engine.createNewGraphFromTargetList(targetToRunOn);

        graphStage.root.setBottom(taskOutput);
    }


    public void initWorkingThread(TaskSettings taskSettings) {
        Thread work = new Thread(() -> {
            if (taskSettings.runFromScratch) {
                graphStage.engine.runTask(taskSettings.task, taskSettings.maxThreads);
            } else {
                graphStage.engine.runTaskIncrementally();
            }
        }, "TaskRunner");
        work.start();
    }

    private void initChangingColorThread() {
        Thread check = new Thread(() -> {
            HashMap<String, AtomicBoolean> flickering = new HashMap<>();
            while (graphStage.engine.isTaskRunning() || graphStage.engine.getTaskRunner() == null) {
                changeColors(flickering);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            changeColors(flickering);
            Platform.runLater(() ->
            {
                runButton.setText("Finished!");
                paused = false;
                alertWhenDone();
            });
        }, "ColorChanger");
        check.start();
    }


    private void pauseResume(ActionEvent actionEvent) {
        if (!graphStage.engine.isTaskRunning()) {
            return;
        }
        if (graphStage.engine.toggleTaskRunning()) {
            runButton.setText("Resume");
            paused = true;
        } else {
            runButton.setText("Pause");
            paused = false;
        }
    }

    private void alertWhenDone() {
        Alert information = new Alert(Alert.AlertType.INFORMATION);
        information.setTitle("Final-Results");
        information.setContentText(graphStage.engine.getResultStatistics());
        information.showAndWait();
    }

    private void alertWarning(String warning) {
        Alert information = new Alert(Alert.AlertType.WARNING);
        information.setTitle("Warning");
        information.setContentText(warning);
        information.showAndWait();
    }

    @Override
    public void reset() {
        super.reset();
        paused = false;
    }
}
