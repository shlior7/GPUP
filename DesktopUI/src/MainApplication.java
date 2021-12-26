/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.brunomnsilva.smartgraph.containers.SmartGraphDemoContainer;
import com.brunomnsilva.smartgraph.graph.*;

import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphProperties;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Queue;

/**
 * @author Liron Blecher
 */
public class MainApplication extends Application {


    @Override
    public void start(Stage primaryStage) {
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
        try {
            load(stage);
            Thread.sleep(2000);
        } catch (Exception e) {
            UI.error(e.getMessage());
        }

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
                    targets.insertEdge(k, t, (k + "->" + t));
                } catch (InvalidVertexException ignored) {
                }
            });
        });
        graphView.update();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    void load(Stage stage) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(stage);

        Engine.load(FileHandler.loadGPUPXMLFile(file));
    }

    void runTask() {

        Simulation simulation = new Simulation(2000, true, 0.6f, 0.3f);

//    if (startFromLastPoint && !Engine.taskAlreadyRan())
//      UI.warning("the graph does not have previous task runs");

        FileHandler.createLogLibrary(simulation.getName());

        Queue<Target> queue = Engine.InitTaskAndGetRunningQueue(false);

        while (!queue.isEmpty()) {
            Target target = queue.poll();
            try {
                UI.log("Start Time: " + DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()), target.name);
                UI.log("Start Task On " + target.name, target.name);
                UI.log("Targets Data: " + target.getUserData(), target.name);
                Engine.runTaskOnTarget(target, simulation);
                UI.log("Finished Time: " + DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()), target.name);
                UI.log("Task Finished with " + target.getResult().toString(), target.name);
                UI.println("--------------------------------\n");
            } catch (IOException e) {
                UI.warning("couldn't log to file");
            } catch (InterruptedException ignored) {
            }
            Engine.addTheDadsThatAllTheirSonsFinishedSuccessfullyToQueue(queue, target);
        }
        Engine.setAllFrozensToSkipped();
        Engine.getStatusesStatistics().forEach((k, v) -> UI.printDivide(k + ": " + v.size() + " : {" + String.join(", ", v) + "}" + "\n"));


    }
}
