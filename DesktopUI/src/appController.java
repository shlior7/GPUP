
import com.brunomnsilva.smartgraph.containers.SmartGraphDemoContainer;
import com.brunomnsilva.smartgraph.graph.Digraph;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.InvalidVertexException;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

public class appController {
    private Engine engine;

    //    @FXML
//    private Button runTaskButton;
//    @FXML
//    private Button showGraphButton;
    @FXML
    void loadGraph(ActionEvent event) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(GraphApplication.getPrimaryStage());

        Engine.load(FileHandler.loadGPUPXMLFile(file));
        engine = new Engine();
    }

    @FXML
    void runTask(ActionEvent event) {
        runTaskConsole();
    }

    @FXML
    void showGraph(ActionEvent event) throws Exception {
        visualGraph();
    }

    void load(Stage stage) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(stage);

        Engine.load(FileHandler.loadGPUPXMLFile(file));
    }

//    public void visualGraph() throws Exception {
//        GraphProperties properties = new GraphProperties("edge.arrow = true\n" + "edge.label = false\n" + "edge.arrowsize = 7\n");
//        GraphPanel<Target> graphView = new GraphPanel<>(Engine.getTargetGraph(), properties);
//        Scene scene = new Scene(new BorderPane(graphView), 1024, 768);
//
//        Stage stage = new Stage(StageStyle.DECORATED);
//        stage.setTitle("JavaFX SmartGraph City Distances");
//        stage.setMinHeight(500);
//        stage.setMinWidth(800);
//        stage.setScene(scene);
//        stage.show();
//
//        graphView.init();
//    }


    public void runTaskConsole() {
        int timeToProcess = UI.promptInt("Please enter the time to process the simulation (milliseconds) ", 0, Integer.MAX_VALUE);
        boolean isRandom = UI.promptBoolean("Please enter if the process time should be random or not\n(the process time enter before is the maximum)");
        float successProbability = UI.promptFloat("Please enter the probability of success");
        float successWithWarningProbability = UI.promptFloat("Please enter the probability of warning given it was successful");
        boolean startFromLastPoint = Engine.validateGraph() && UI.promptBoolean("Do you want to start the task on the graph from the last point");

        Simulation simulation = new Simulation(timeToProcess, isRandom, successProbability, successWithWarningProbability);
        int parallel = UI.promptInt("Please enter the number of threads ", 0, Integer.MAX_VALUE);

        if (startFromLastPoint && !Engine.taskAlreadyRan())
            UI.warning("the graph does not have previous task runs");


        Thread work = new Thread(() -> {
            engine.runTask(simulation, parallel);
        }, "Task Running");
        System.out.println("started");
        work.start();
    }

    public void visualGraph() {
        Digraph<String, String> targets = new DigraphEdgeList<>();
        StringBuilder Props = new StringBuilder("");
        Props.append("edge.arrow = true").append("\n");
        Props.append("edge.label = false").append("\n");
        Props.append("edge.arrowsize = 7").append("\n");

        SmartGraphProperties properties = new SmartGraphProperties(Props.toString());
        SmartGraphPanel<String, String> graphView = new SmartGraphPanel<>(targets, properties, new SmartCircularSortedPlacementStrategy());

        Scene scene = new Scene(new SmartGraphDemoContainer(graphView), 1024, 768);

        Stage stage = new Stage(StageStyle.UNIFIED);
        stage.setTitle("JavaFX SmartGraph City Distances");
        stage.setMinHeight(500);
        stage.setMinWidth(800);
        stage.setScene(scene);
        stage.show();

        graphView.init();


        Engine.getTargetGraph().getAllElementMap().keySet().forEach(t -> {
            try {
                targets.insertVertex(t);
            } catch (InvalidVertexException ignored) {
            }
        });


        Engine.getTargetGraph().getAdjNameMap().forEach((k, v) -> {
            v.forEach(t -> {
                try {
                    targets.insertEdge(k, t.name, (k + "->" + t));
                } catch (InvalidVertexException ignored) {
                }
            });
        });
        graphView.update();

    }

    public void toggleTask() {
        engine.toggleTaskRunning();
    }
}
