package app.main;


import app.dashboard.Dashboard;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import okhttp3.HttpUrl;
import types.Admin;
import utils.Constants;
import utils.http.HttpClientUtil;
import utils.http.SimpleCallBack;

import java.io.IOException;

public class AdminApp extends Application {
    @FXML
    public TextField name_worker;

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

        Dashboard controller = Dashboard.createDashboard();
        Admin user = new Admin(name_worker.getText());
        String finalUrl = HttpUrl
                .parse(Constants.LOGIN_PATH)
                .newBuilder()
                .addQueryParameter(Constants.USERNAME, user.getName())
                .addQueryParameter(Constants.ROLE, user.getRole())
                .build()
                .toString();
        System.out.println("finalUrl " + finalUrl);
        HttpClientUtil.runAsync(finalUrl, new SimpleCallBack((s) -> controller.show()));
    }

}
