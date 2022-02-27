package app.components;

import TargetGraph.TargetGraph;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

public class taskSettingsManager {

    public TaskSettings open(TargetGraph tasksGraph) {
        TaskSettings taskSettings = null;
        try {
            FXMLLoader loader = new FXMLLoader();
            Pane root = loader.load(getClass().getResource("taskSettings.fxml").openStream());
            taskSettings = loader.getController();
            taskSettings.init(tasksGraph, tasksGraph.getPrices().keySet());
            taskSettings.showAndReturn(root);
            System.out.println("tasksGraph = " + taskSettings.Task.getTaskName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return taskSettings;
    }
}
