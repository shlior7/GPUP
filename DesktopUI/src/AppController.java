

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

import java.io.File;

public class AppController {
    private Engine engine;

    public AppController() throws Exception {
//        File file = new File("ex2-cycle.xml");
////        Engine.load(FileHandler.loadGPUPXMLFile(file));
//        engine = new Engine();
    }

    @FXML
    void loadGraph(ActionEvent event) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        FileChooser.ExtensionFilter xmlfilter = new FileChooser.ExtensionFilter(
                "XML Files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(xmlfilter);
        File file = fileChooser.showOpenDialog(null);
//        File file = new File("ex2-big.xml");
        try {
            Engine.load(FileHandler.loadGPUPXMLFile(file));
            engine = new Engine();
        } catch (Exception e) {
            Alert errorMessege = new Alert(Alert.AlertType.ERROR);
            errorMessege.setTitle("File loading error");
            errorMessege.setContentText(e.getMessage());
            errorMessege.show();
        }
    }

    @FXML
    void showGraph(ActionEvent event) {
        if (engine == null) {
            Alert errorMessege = new Alert(Alert.AlertType.ERROR);
            errorMessege.setTitle("Error");
            errorMessege.setContentText("You need to load graph first!");
            errorMessege.show();
        } else {
            GraphStage graphController = new GraphStage(engine);
            graphController.showGraph();
        }
    }
}
