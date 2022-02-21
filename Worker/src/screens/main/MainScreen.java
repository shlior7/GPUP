package screens.main;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import okhttp3.HttpUrl;
import screens.dashboard.Dashboard;
import types.Worker;
import utils.Constants;
import utils.http.HttpClientUtil;
import utils.http.SimpleCallBack;

import java.io.IOException;

import static utils.Utils.alertWarning;

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


    private boolean checkValidRegistration(){
        try {
            int num = Integer.parseInt(num_threads.getText());
            if(num <= 5 && num >= 1) {
                return true;
            }
            else{
                alertWarning("The number of threads should be a number between 1 to 5.");
            }
        } catch(NumberFormatException e){
            alertWarning("The number of threads should be a number between 1 to 5.");
        }
        return false;
    }


    public void register(ActionEvent actionEvent) throws IOException {
        System.out.println(name_worker.getText());
        System.out.println(num_threads.getText());

        if(checkValidRegistration()) {
            Worker user = new Worker(name_worker.getText(), Integer.parseInt(num_threads.getText()));
            Dashboard controller = Dashboard.createDashboard(user);
            String finalUrl = HttpUrl
                    .parse(Constants.LOGIN_PAGE)
                    .newBuilder()
                    .addQueryParameter(Constants.USERNAME, user.getName())
                    .addQueryParameter(Constants.ROLE, user.getRole())
                    .addQueryParameter(Constants.THREADS, String.valueOf(user.getThreads()))
                    .build()
                    .toString();
            System.out.println("finalUrl " + finalUrl);
            HttpClientUtil.runAsync(finalUrl, new SimpleCallBack((s) -> controller.show()));
        }
    }

}
