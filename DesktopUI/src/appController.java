

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class appController {
    private Engine engine;
    private GraphPanel<Target> graphView;

    public appController() throws Exception {
        File file = new File("ex2-big.xml");
        Engine.load(FileHandler.loadGPUPXMLFile(file));
        engine = new Engine();

    }

    @FXML
    void loadGraph(ActionEvent event) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
//        File file = fileChooser.showOpenDialog(GraphApplication.getPrimaryStage());
        File file = new File("ex2-big.xml");
        Engine.load(FileHandler.loadGPUPXMLFile(file));
        engine = new Engine();

    }

    @FXML
    void showGraph(ActionEvent event) throws Exception {
        GraphStage graphController = new GraphStage(engine);
        graphController.showGraph();
//        visualGraph();
    }

    void load(Stage stage) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(stage);
        Engine.load(FileHandler.loadGPUPXMLFile(file));
    }

    public void visualGraph() {
        GraphProperties properties = new GraphProperties("edge.arrow = true\n" + "edge.label = false\n" + "edge.arrowsize = 7\n");
        graphView = new GraphPanel<>(Engine.getTargetGraph(), properties, this::onVertexClicked);

        BorderPane root = new BorderPane();
        root.setCenter(graphView);
        VBox vBox = new VBox(5);
        vBox.setPrefWidth(100);
        Button task = new Button("run task");
        AnchorPane anchorPane = new AnchorPane(task);
        AnchorPane.setLeftAnchor(task, 0.0);
        AnchorPane.setRightAnchor(task, 0.0);
        task.setOnAction(this::runTask);
        vBox.getChildren().add(anchorPane);
        root.setRight(vBox);

        Scene scene = new Scene(root, 2048, 1800);

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("JavaFX SmartGraph City Distances");
        stage.setMinHeight(500);
        stage.setMinWidth(800);
        stage.setScene(scene);

        stage.show();
        graphView.init();

    }

    private void runTask(ActionEvent actionEvent) {
        runTaskConsole();
    }


    public void runTaskConsole() {
//        int timeToProcess = UI.promptInt("Please enter the time to process the simulation (milliseconds) ", 0, Integer.MAX_VALUE);
//        boolean isRandom = UI.promptBoolean("Please enter if the process time should be random or not\n(the process time enter before is the maximum)");
//        float successProbability = UI.promptFloat("Please enter the probability of success");
//        float successWithWarningProbability = UI.promptFloat("Please enter the probability of warning given it was successful");
//        boolean startFromLastPoint = Engine.validateGraph() && UI.promptBoolean("Do you want to start the task on the graph from the last point");
//
//        Simulation simulation = new Simulation(timeToProcess, isRandom, successProbability, successWithWarningProbability);
        Simulation simulation = new Simulation(5000, false, 0.8f, 0.3f);
//        int parallel = UI.promptInt("Please enter the number of threads ", 0, Integer.MAX_VALUE);
        int parallel = 10;
//        if (false && !Engine.taskAlreadyRan())
//            UI.warning("the graph does not have previous task runs");


        Thread work = new Thread(() -> {
//            engine.runTask(simulation, parallel);
        }, "Task Running");
        System.out.println("started");
        work.start();


        Thread check = new Thread(() -> {
            HashMap<String, AtomicBoolean> flickering = new HashMap<>();
            while (engine.isTaskRunning()) {
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
    }

    public void changeColors(HashMap<String, AtomicBoolean> flickering) {
        engine.getAllTargets().forEach((name, target) -> {
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
        System.out.println(engine.onTargetClicked(target));
    }


//    public void visualGraph() {
//        Digraph<String, String> targets = new DigraphEdgeList<>();
//        StringBuilder Props = new StringBuilder("");
//        Props.append("edge.arrow = true").append("\n");
//        Props.append("edge.label = false").append("\n");
//        Props.append("edge.arrowsize = 7").append("\n");
//
//        SmartGraphProperties properties = new SmartGraphProperties(Props.toString());
//        SmartGraphPanel<String, String> graphView = new SmartGraphPanel<>(targets, properties, new SmartCircularSortedPlacementStrategy());
//
//        Scene scene = new Scene(new SmartGraphDemoContainer(graphView), 1024, 768);
//
//        Stage stage = new Stage(StageStyle.UNIFIED);
//        stage.setTitle("JavaFX SmartGraph City Distances");
//        stage.setMinHeight(500);
//        stage.setMinWidth(800);
//        stage.setScene(scene);
//        stage.show();
//
//        graphView.init();
//
//
//        Engine.getTargetGraph().getAllElementMap().keySet().forEach(t -> {
//            try {
//                targets.insertVertex(t);
//            } catch (InvalidVertexException ignored) {
//            }
//        });
//
//
//        Engine.getTargetGraph().getAdjNameMap().forEach((k, v) -> {
//            v.forEach(t -> {
//                try {
//                    targets.insertEdge(k, t.name, (k + "->" + t));
//                } catch (InvalidVertexException ignored) {
//                }
//            });
//        });
//        graphView.update();
//    }

    public void toggleTask() {
        engine.toggleTaskRunning();
    }


}
