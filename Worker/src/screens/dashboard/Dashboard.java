package screens.dashboard;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import app.components.Task.TaskComponent;
import app.components.Task.TaskData;
import app.components.User.User;
import app.components.User.UserComponent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Dashboard extends Stage implements Initializable {

    public static Dashboard createDashboard() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Dashboard.class.getResource("worker_dashboard.fxml"));
        BorderPane root = fxmlLoader.load();
        Dashboard dashboard = fxmlLoader.getController();
        dashboard.setScene(new Scene(root, 2048, 1800));
        return dashboard;
    }

    @FXML
    VBox tasks_container;

    @FXML
    VBox users_container;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void addTask(TaskData taskData) {
        TaskComponent taskComponent = new TaskComponent(taskData);
        tasks_container.getChildren().add(taskComponent);
    }

    public void addUser(User user) {
        UserComponent userComponent = new UserComponent(user);
        tasks_container.getChildren().add(userComponent);
    }
}
