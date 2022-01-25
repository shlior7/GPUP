import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.AnchorPane;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class RunTask extends SideAction {
    private TargetsCheckComboBox<String> targetsComboBox;
    private ActionButton runButton;
    private boolean choose;
    private TextArea taskOutput;

    public RunTask(GraphStage graphStage, Runnable onOpenSettings) {
        super("Run Task", graphStage, onOpenSettings);
        runButton = new ActionButton();
        setOnAction(this::runTask);
        choose = true;
        taskOutput = new TextArea();
        targetsComboBox = new TargetsCheckComboBox<>(graphStage.engine.getAllTargets().values().stream().map(Target::getName).collect(Collectors.toList()), this::onAdd, this::onRemove);
        this.settings.getChildren().addAll(new AnchoredButton(runButton), new AnchoredNode(targetsComboBox));
        AnchorPane.setLeftAnchor(taskOutput, 0.0);
        AnchorPane.setRightAnchor(taskOutput, 0.0);
        taskOutput.minHeight(100);
        taskOutput.maxHeight(100);
        taskOutput.setStyle("-fx-font-size: 2em;");
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

    void runTask(ActionEvent event) {
        if (graphStage.choosingController.isChoosing())
            return;
        onOpenSettings.run();

        TaskSettings taskSettings = TaskSettings.createTaskSettings();
        taskSettings.showAndReturn(graphStage.engine.getMaxThreads(), graphStage);

        if (!taskSettings.submitted)
            return;

        graphStage.choosingController.setChoosingState(true);
        graphStage.choosingController.setOnChoose(this::onChoose);
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
        graphStage.choosingController.setChoosingState(false);
        System.out.println("targets = " + targetToRunOn);
        graphStage.graphView.hideEdges(targetToRunOn);
//        graphStage.engine.createNewGraphFromTargetList(targetToRunOn);
        taskOutput.textProperty().bind(Bindings.createStringBinding(() -> taskOutput.getText() + "\n" + getInstantTime() + ".   " + graphStage.engine.getTaskRunner().getTaskOutput().get(), graphStage.engine.getTaskRunner().getTaskOutput()));


        graphStage.root.setBottom(taskOutput);

        Thread work = new Thread(() -> {
            graphStage.engine.runTask(taskSettings.task, targetToRunOn, taskSettings.maxThreads, taskSettings.runFromScratch);
        }, "Task Running");
        System.out.println("started");
        work.start();


        Thread check = new Thread(() -> {
            HashMap<String, AtomicBoolean> flickering = new HashMap<>();
            while (graphStage.engine.isTaskRunning() || graphStage.engine.getTaskRunner() == null) {
                System.out.println("changing colors");
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
            });
        }, "Task Running");
        check.start();

        runButton.setText("Pause");
        runButton.setOnAction(this::pauseResume);
    }

    private void pauseResume(ActionEvent actionEvent) {
        if (!graphStage.engine.isTaskRunning()) {
            return;
        }
        if (graphStage.engine.toggleTaskRunning())
            runButton.setText("Resume");
        else
            runButton.setText("Pause");
    }
}
