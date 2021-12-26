
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
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Queue;

public class appController {

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

    public void visualGraph() throws Exception {
        GraphProperties properties = new GraphProperties("edge.arrow = true\n" + "edge.label = false\n" + "edge.arrowsize = 7\n");
        GraphPanel<Target> graphView = new GraphPanel<>(Engine.getTargetGraph(), properties);
        Scene scene = new Scene(new BorderPane(graphView), 1024, 768);

        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle("JavaFX SmartGraph City Distances");
        stage.setMinHeight(500);
        stage.setMinWidth(800);
        stage.setScene(scene);
        stage.show();

        graphView.init();
    }


    public void runTaskConsole() {
        int timeToProcess = UI.promptInt("Please enter the time to process the simulation (milliseconds) ", 0, Integer.MAX_VALUE);
        boolean isRandom = UI.promptBoolean("Please enter if the process time should be random or not\n(the process time enter before is the maximum)");
        float successProbability = UI.promptFloat("Please enter the probability of success");
        float successWithWarningProbability = UI.promptFloat("Please enter the probability of warning given it was successful");
        boolean startFromLastPoint = Engine.validateGraph() && UI.promptBoolean("Do you want to start the task on the graph from the last point");

        Simulation simulation = new Simulation(timeToProcess, isRandom, successProbability, successWithWarningProbability);

        if (startFromLastPoint && !Engine.taskAlreadyRan())
            UI.warning("the graph does not have previous task runs");

        FileHandler.createLogLibrary(simulation.getName());

        Queue<Target> queue = Engine.InitTaskAndGetRunningQueue(startFromLastPoint);

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
