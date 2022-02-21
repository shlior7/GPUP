//package graphApp.actions.task;
//
//
//import TargetGraph.Result;
//import TargetGraph.Status;
//import TargetGraph.Target;
//import graphApp.GraphPane;
//import graphApp.actions.SideAction;
//import graphApp.components.ActionButton;
//import graphApp.components.AnchoredNode;
//import graphApp.components.TargetsCheckComboBox;
//import javafx.application.Platform;
//import javafx.event.ActionEvent;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.control.Alert;
//import javafx.scene.control.ProgressBar;
//import javafx.scene.control.TextArea;
//import javafx.scene.layout.AnchorPane;
//import javafx.scene.layout.GridPane;
//
//import java.io.IOException;
//import java.net.URL;
//import java.time.LocalTime;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.stream.Collectors;
//
//public class RunTask extends SideAction {
//    private final TargetsCheckComboBox<String> targetsComboBox;
//    private final ActionButton runButton;
//    private final ProgressBar progressBar;
//    private boolean choose;
//    private TextArea taskOutput;
//    private boolean paused;
//
//    public RunTask(GraphPane graphPane) {
//        super("Run Task", graphPane);
//
//        //Admin
//        this.runButton = new ActionButton();
//        this.targetsComboBox = new TargetsCheckComboBox<>(graphPane.graph.getVerticesMap().values().stream().map(Target::getName).collect(Collectors.toList()), this::onAdd, this::onRemove);
//        this.settings.getChildren().addAll(new AnchoredNode(runButton), new AnchoredNode(targetsComboBox));
//        this.choose = true;
//        createLogTextArea();
//        setOnAction(this::chooseTargets);
//
//        //Both
//        this.paused = false;
//        createColorMap();
//        this.progressBar = new ProgressBar();
//        this.progressBar.progressProperty().bind(graphPane.engine.getTaskRunner().getProgress());///server
//        settings.getChildren().add(new AnchorPane(progressBar));
//    }
//
//
//    ///Admin
//    public void onAdd(String name) {
//        if (!choose) {
//            choose = true;
//            return;
//        }
//        choose = false;
//        graphPane.choosingController.manualClick(graphPane.graph.getVerticesMap().get(name));
//        choose = true;
//    }
//
//    public void onRemove(String name) {
//        if (!choose) {
//            choose = true;
//            return;
//        }
//        choose = false;
//        graphPane.choosingController.manualClick(graphPane.graph.getVerticesMap().get(name));
//        choose = true;
//    }
//
//    void chooseTargets(ActionEvent event) {
//        if (graphPane.choosingController.isChoosing() || paused)
//            return;
//        TaskSettings taskSettings = TaskSettings.createTaskSettings(graphPane.graph);
//        taskSettings.showAndReturn(graphPane.graph.getMaxThreads(), graphPane);
//
//        if (!taskSettings.submitted)
//            return;
//
//        graphPane.choosingController.setChoosingState(true);
//        graphPane.choosingController.setOnChoose(this::onChoose);
//
//        if (!taskSettings.runFromScratch) {
//            graphPane.choosingController.chooseTargets(graphPane.graph.getCurrentTargets());
//            taskRun(taskSettings);
//            settings.setVisible(true);
//            return;
//        }
//
//        if (taskSettings.chooseAll)
//            graphPane.choosingController.all(null);
//
//        runButton.setOnAction((ea) -> taskRun(taskSettings));
//        runButton.setText("Start");
//        settings.setVisible(true);
//        targetsComboBox.setDisable(false);
//    }
//
//    public void onChoose(Target target) {
//        if (choose) {
//            choose = false;
//            targetsComboBox.getCheckModel().toggleCheckState(target.name);
//        }
//    }
//
//    private void BeforeRunning(TaskSettings taskSettings, Set<Target> targetToRunOn) {
//        if (taskSettings.runFromScratch) {
//            graphPane.graphView.reset();
//        } else {
//            targetToRunOn = targetToRunOn.stream().filter(target -> target.getResult() == Result.Failure || target.getStatus() == Status.SKIPPED).collect(Collectors.toSet());
//        }
//        graphPane.choosingController.clear(null);
//        graphPane.choosingController.setChoosingState(false);
//        targetsComboBox.setDisable(true);
//        graphPane.graphView.hideEdges(targetToRunOn);
//        targetToRunOn.forEach(t -> t.init(""));
//        graphPane.graph.createNewGraphFromTargetList(targetToRunOn);
//        graphPane.root.setBottom(taskOutput);
//    }
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
//
//    @Override
//    public void reset() {
//        super.reset();
//        paused = false;
//    }
//
//    ////BOTH
//    public synchronized void changeColors(HashMap<String, AtomicBoolean> flickering) {
//        graphPane.graph.getAdjacentMap().keySet().forEach(name -> {
//            Target target = graphPane.graph.getVerticesMap().get(name);
//            Status status = target.getStatus();
//            String stroke = status.getColor();
//            String fill = target.getResult().getColor();
//            if (status == Status.FINISHED)
//                stroke = fill;
//            if (status == Status.IN_PROC01ESS) {
//                flickering.putIfAbsent(name, new AtomicBoolean(false));
//                stroke = flickering.get(name).get() ? "yellow" : "gold";
//                flickering.get(name).set(!flickering.get(name).get());
//            }
//            graphPane.graphView.getStylableVertex(target).setStyle("-fx-stroke: " + stroke + ";" + "-fx-fill: " + fill + ";");
//        });
//    }
//
//    public String getInstantTime() {
//        taskOutput.setScrollTop(Double.MIN_VALUE);
//        taskOutput.deselect();
//        return LocalTime.now().toString();
//    }
//
//
//    public void afterRunning() {
//        runButton.setText("Finished!");
//        paused = false;
////        alertWhenDone();
//    }
//
//
//    private void initChangingColorThread() {
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
//
//
////    private void alertWhenDone() {
////        Alert information = new Alert(Alert.AlertType.INFORMATION);
////        information.setTitle("Final-Results");
////        information.setContentText(graphStage.engine.getResultStatistics());
////        information.showAndWait();
////    }
//
//    private void alertWarning(String warning) {
//        Alert information = new Alert(Alert.AlertType.WARNING);
//        information.setTitle("Warning");
//        information.setContentText(warning);
//        information.showAndWait();
//    }
//
//    public void createColorMap() {
//        FXMLLoader fxmlLoader = new FXMLLoader();
//        URL taskUrl = TaskSettings.class.getResource("map.fxml");
//        fxmlLoader.setLocation(taskUrl);
//        try {
//            GridPane map = fxmlLoader.load(taskUrl.openStream());
//            map.setMaxWidth(50);
//            settings.getChildren().add(map);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void createLogTextArea() {
//        taskOutput = new TextArea();
//        AnchorPane.setLeftAnchor(taskOutput, 0.0);
//        AnchorPane.setRightAnchor(taskOutput, 0.0);
//        AnchorPane.setLeftAnchor(progressBar, 0.0);
//        AnchorPane.setRightAnchor(progressBar, 0.0);
//        taskOutput.minHeight(100);
//        taskOutput.maxHeight(100);
//        taskOutput.setStyle("-fx-font-size: 2em;");
//
//        graphPane.engine.getTaskRunner().getTaskOutput().addListener((observable, oldValue, newValue) -> {///server
//            if (!Objects.equals(oldValue, newValue))
//                Platform.runLater(() -> taskOutput.appendText("\n" + getInstantTime() + ".   " + newValue));
//        });
//    }
//}
