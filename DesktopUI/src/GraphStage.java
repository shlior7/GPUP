import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class GraphStage extends Stage {
    public final Engine engine;
    public GraphPanel<Target> graphView;
    public final BorderPane root;
    private boolean choosing = false;
    private final HashMap<Target, Boolean> chosen;
    private final Button actionButton;
    public ChoosingController choosingController;
    private HBox findPathHbox;
    private SideController sideController;


    public GraphStage(Engine engine) {
        this.choosingController = new ChoosingController(this);
        this.chosen = new HashMap<>();
        this.engine = engine;
        this.initStyle(StageStyle.DECORATED);
        this.setTitle("Target Graph");
        this.setMinHeight(500);
        this.setMinWidth(800);
        root = new BorderPane();
        this.findPathHbox = createFindPathHBox();
        this.setScene(new Scene(root, 2048, 1800));
        this.actionButton = new Button("start");
        actionButton.setVisible(false);
        actionButton.setMinHeight(50);

        DropShadow borderGlow = new DropShadow();
        borderGlow.setOffsetY(0f);
        borderGlow.setOffsetX(0f);
        borderGlow.setColor(Color.GOLD);
        borderGlow.setWidth(20);
        borderGlow.setHeight(20);

        actionButton.setEffect(borderGlow);
        sideController = new SideController(this);
        root.setRight(sideController);
        CreateGraph();

    }

    public void showGraph() {
        this.show();
        graphView.init();
    }

    public synchronized void changeColors(HashMap<String, AtomicBoolean> flickering) {
        engine.getAdjacentMap().keySet().forEach(name -> {
            Target target = engine.getAllTargets().get(name);
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
            graphView.getStylableVertex(target).setStyle("-fx-stroke: " + stroke + ";" + "-fx-fill: " + fill + ";");
        });
    }

    public void onVertexClicked(Target target) {
        choosingController.onClicked(target);
    }

    public HBox createFindPathHBox() {
        TextField source = new TextField();
        TextField dest = new TextField();

        Button opposite = new Button("<-->");
        opposite.setMinWidth(50);
        opposite.setMaxHeight(source.getHeight());
        HBox hBox = new HBox();
        hBox.getChildren().addAll(source, opposite, dest);
        return hBox;
    }

    public void toggleTask() {
        engine.toggleTaskRunning();
    }

    public void CreateGraph() {
        GraphProperties properties = new GraphProperties("edge.arrow = true\n" + "edge.label = false\n" + "edge.arrowsize = 7\n");
        graphView = new GraphPanel<>(Engine.getTargetGraph(), properties, this::onVertexClicked);
        GraphContainer graphContainer = new GraphContainer(graphView);
        root.setCenter(graphContainer);
    }
//
//    public void CreateButtonsVBox() {
//        VBox vBox = new VBox(20);
//        vBox.setPrefWidth(200);
//
//        vBox.getChildren().add(createAnchoredButton("Run Task", this::runTask));
//        vBox.getChildren().add(createAnchoredButton("Find Path", this::findPath));
//        vBox.getChildren().add(createAnchoredButton("Find Circuit", this::findCircuit));
//        vBox.getChildren().add(createAnchoredButton("What If", this::whatIf));
//        vBox.getChildren().add(createAnchoredButton("Reset", this::reset));
//        vBox.getChildren().add(createAnchoredButton(actionButton));
//        vBox.getChildren().add(findPathHbox);
//
//
//        root.setRight(vBox);
//    }


    public AnchorPane createAnchoredButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> value) {
        Button task = new Button(text);
        AnchorPane anchorPane = new AnchorPane(task);
        AnchorPane.setLeftAnchor(task, 0.0);
        AnchorPane.setRightAnchor(task, 0.0);
        task.setOnAction(value);
        return anchorPane;
    }

    public AnchorPane createAnchoredButton(Button task) {
        AnchorPane anchorPane = new AnchorPane(task);
        AnchorPane.setLeftAnchor(task, 0.0);
        AnchorPane.setRightAnchor(task, 0.0);
        return anchorPane;
    }

    void runTask(ActionEvent event) {
        if (choosingController.isChoosing())
            return;

        TaskSettings ts = new TaskSettings(10);
        ts.showAndWait();
        if (!ts.submitted)
            return;

        choosingController.setChoosingState(true);

        actionButton.setOnAction((ea) -> taskRun(ts));
        actionButton.setText("Start");
        actionButton.setVisible(true);
//        ((VBox) root.getRight()).getChildren().add(0, createAnchoredButton(start_pause_resume));
    }


    public void setChoosingState(boolean choosingState) {
        this.choosing = choosingState;
        graphView.setPressable(choosingState);
    }

    public HBox createHboxChoosingButtons() {
        HBox hBox = new HBox();
        Button clear = new Button("clear");
        clear.setOnAction(this::clear);
        Button whatIfDepends = new Button("What If Depends On");
        whatIfDepends.setOnAction(this::whatIfDepends);
        Button whatIfRequired = new Button("What If Required For");
        whatIfRequired.setOnAction(this::whatIfRequired);
        Button cancel = new Button("cancel");
        cancel.setOnAction(this::cancel);

        hBox.getChildren().addAll(clear, whatIfDepends, whatIfRequired, cancel);
        return hBox;
    }

    private void whatIfRequired(ActionEvent actionEvent) {
    }

    private void cancel(ActionEvent actionEvent) {
        clear(actionEvent);
        setChoosingState(false);
        reset(actionEvent);
    }

    private void whatIfDepends(ActionEvent actionEvent) {
    }

    private synchronized void clear(ActionEvent actionEvent) {
        chosen.forEach((target, pressed) -> {
            if (pressed) {
                graphView.getGraphVertex(target).setVertexStyleToDefault();
                chosen.put(target, false);
            }
        });
        System.out.println("chosen " + chosen);
    }

    public void taskRun(TaskSettings taskSettings) {
        Set<Target> targetToRunOn = choosingController.getChosenTargets();
        choosingController.setChoosingState(false);
        System.out.println("targets = " + targetToRunOn);
        graphView.hideEdges(targetToRunOn);
        engine.createNewGraphFromTargetList(targetToRunOn);

        Thread work = new Thread(() -> {
            engine.runTask(taskSettings.task, targetToRunOn, taskSettings.maxThreads, taskSettings.runFromScratch);
        }, "Task Running");
        System.out.println("started");
        work.start();


        Thread check = new Thread(() -> {
            HashMap<String, AtomicBoolean> flickering = new HashMap<>();
            while (engine.isTaskRunning() || engine.getTaskRunner() == null) {
                System.out.println("changing colors");
                changeColors(flickering);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            changeColors(flickering);
        }, "Task Running");
        check.start();

        actionButton.setText("Pause");
        actionButton.setOnAction(this::pauseResume);
    }

    private void pauseResume(ActionEvent actionEvent) {
        if (!engine.isTaskRunning()) {
            actionButton.setText("Finished!");
            return;
        }
        if (engine.toggleTaskRunning())
            actionButton.setText("Resume");
        else
            actionButton.setText("Pause");
    }

    public void reset(ActionEvent actionEvent) {
        if (engine.isTaskRunning())
            return;
        graphView.reset();
        engine.reset();
        choosingController.setChoosingState(false);
        actionButton.setVisible(false);
    }

    public void findPath(ActionEvent event) {
//        setChoosingState(true);
        choosingController.setChoosingState(true, 2);
        actionButton.setText("Find");
        actionButton.setVisible(true);
        findPathHbox.setVisible(true);
        actionButton.setOnAction((actionEvent) -> {

        });
    }

}
