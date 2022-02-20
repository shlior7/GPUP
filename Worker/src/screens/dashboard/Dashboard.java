package screens.dashboard;

import TargetGraph.TargetGraph;
//import app.utils.Constants;
//import app.utils.http.HttpClientUtil;
//import app.utils.http.SimpleCallBack;
import com.google.gson.reflect.TypeToken;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import types.GraphInfo;
import types.UserInfo;
import types.Worker;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

//import static app.utils.Constants.GSON_INSTANCE;


public class Dashboard extends Stage implements Initializable {

    private Worker worker;

    @FXML
    private TableView<?> missionTable;

    @FXML
    private TableColumn<?, ?> selectedListMissions;

    @FXML
    private ListView<?> missionsList;

    @FXML
    public TableView<UserInfo> UsersTable;

    @FXML
    public TableView<GraphInfo> graphTable;

    @FXML
    public TableView taskTable;

    public static Dashboard createDashboard(Worker worker) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Dashboard.class.getResource("worker_dashboard.fxml"));
        ScrollPane root = fxmlLoader.load();
        Dashboard dashboard = fxmlLoader.getController();
        dashboard.setWorker(worker);
        dashboard.setScene(new Scene(root, 2048, 1800));
        //dashboard.getUsers();
        return dashboard;
    }
    public void setWorker(Worker worker){
        this.worker = worker;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }


//    public void getGraphs() {
//        String finalUrl = HttpUrl
//                .parse(Constants.GET_GRAPHS_ALL)
//                .newBuilder()
//                .build()
//                .toString();
//        System.out.println("finalUrl " + finalUrl);
//
//        HttpClientUtil.runAsync(finalUrl, new SimpleCallBack((graphJson) -> {
//            GraphInfo[] graphs = GSON_INSTANCE.fromJson(graphJson, GraphInfo[].class);
//            if (graphs != null) setGraphTable(graphs);
//        }));
//    }

    private void setGraphTable(GraphInfo[] graphs) {
        graphTable.getItems().addAll(graphs);
    }

//    public void getUsers() {
//        String finalUrl = HttpUrl
//                .parse(Constants.GET_USERS_ALL)
//                .newBuilder()
//                .build()
//                .toString();
//        System.out.println("finalUrl " + finalUrl);
//
//        HttpClientUtil.runAsync(finalUrl, new SimpleCallBack((graphJson) -> {
//            UserInfo[] users = GSON_INSTANCE.fromJson(graphJson, UserInfo[].class);
//            System.out.println(Arrays.toString(users));
//            if (users != null) setUserTable(users);
//        }));
//    }

    private void setUserTable(UserInfo[] users) {
        UsersTable.getItems().addAll(users);
    }

}
