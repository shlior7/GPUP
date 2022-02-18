package screens.main;


import app.components.User.Worker;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import app.components.Task.TaskData;
import app.components.Task.TaskStatus;
import app.components.User.User;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import screens.dashboard.Dashboard;
import utils.Constants;
import utils.http.HttpClientUtil;

import java.io.IOException;
import java.util.HashMap;

import static utils.Constants.GSON_INSTANCE;

public class MainScreen extends Application {
    @FXML
    public TextField name_worker;
    @FXML
    public TextField num_threads;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main_register.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 800, 500);

        root.setStyle("-fx-background-image: url(/resources/main_background.jpeg);-fx-background-size: cover;");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public void register(ActionEvent actionEvent) throws IOException {
        System.out.println(name_worker.getText());
        System.out.println(num_threads.getText());

        Dashboard controller = Dashboard.createDashboard();
        Worker user1 = new Worker("Shmuel", 5);

        TaskData task1 = new TaskData("shit", user1, "Simulation", 15, new HashMap<String, String[]>() {{
            put("leaf", new String[]{"A", "B"});
            put("middle", new String[]{"C", "D"});
            put("root", new String[]{"R", "F"});
            put("independent", new String[]{"G", "H", "H", "H", "H  "});
        }}, 15, TaskStatus.FINISHED, 15, true);

        String finalUrl = HttpUrl
                .parse(Constants.LOGIN_PAGE)
                .newBuilder()
                .build()
                .toString();
        System.out.println("finalUrl " + finalUrl);
        HttpClientUtil.runAsyncBody(finalUrl, GSON_INSTANCE.toJson(user1), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        System.out.println("Something went wrong: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.code() != 200) {
                    Platform.runLater(() ->
                            System.out.println("Something went wrong: " + responseBody)
                    );
                } else {
                    Platform.runLater(() -> {
                        System.out.println("OK " + responseBody);
                    });
                }
            }
        });
//        controller.addTask(task1);
//        controller.show();
    }

}
