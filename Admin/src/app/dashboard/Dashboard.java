package app.dashboard;

import TargetGraph.TargetGraph;
import app.utils.Constants;
import app.utils.http.HttpClientUtil;
import app.utils.http.SimpleCallBack;
import com.google.gson.reflect.TypeToken;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import types.GraphInfo;
import types.UserInfo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import static app.utils.Constants.GSON_INSTANCE;

public class Dashboard extends Stage implements Initializable {

    @FXML
    public TableView<UserInfo> UsersTable;
    @FXML
    public TableView<GraphInfo> graphTable;
    @FXML
    public TableView taskTable;

    public static Dashboard createDashboard() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Dashboard.class.getResource("admin_dashboard.fxml"));
        TabPane root = fxmlLoader.load();
        Dashboard dashboard = fxmlLoader.getController();
        dashboard.setScene(new Scene(root, 2048, 1800));
        dashboard.getUsers();
        return dashboard;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }


    public void loadXml(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        FileChooser.ExtensionFilter xmlfilter = new FileChooser.ExtensionFilter(
                "XML Files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(xmlfilter);
        File file = fileChooser.showOpenDialog(null);

        String finalUrl = HttpUrl
                .parse(Constants.UPLOAD_XML_PATH)
                .newBuilder()
                .build()
                .toString();
        System.out.println("finalUrl " + finalUrl);

        HttpClientUtil.runAsyncBody(finalUrl, RequestBody.create(MediaType.parse("text/xml"), file), new SimpleCallBack(System.out::println));
        getGraphs();
    }

    public void getGraphs() {
        String finalUrl = HttpUrl
                .parse(Constants.GET_GRAPHS_ALL)
                .newBuilder()
                .build()
                .toString();
        System.out.println("finalUrl " + finalUrl);

        HttpClientUtil.runAsync(finalUrl, new SimpleCallBack((graphJson) -> {
            GraphInfo[] graphs = GSON_INSTANCE.fromJson(graphJson, GraphInfo[].class);
            if (graphs != null) setGraphTable(graphs);
        }));
    }

    private void setGraphTable(GraphInfo[] graphs) {
        graphTable.getItems().addAll(graphs);
    }

    public void getUsers() {
        String finalUrl = HttpUrl
                .parse(Constants.GET_USERS_ALL)
                .newBuilder()
                .build()
                .toString();
        System.out.println("finalUrl " + finalUrl);

        HttpClientUtil.runAsync(finalUrl, new SimpleCallBack((graphJson) -> {
            UserInfo[] users = GSON_INSTANCE.fromJson(graphJson, UserInfo[].class);
            System.out.println(Arrays.toString(users));
            if (users != null) setUserTable(users);
        }));
    }

    private void setUserTable(UserInfo[] users) {
        UsersTable.getItems().addAll(users);
    }

    public void runIncremental(ActionEvent actionEvent) {
    }

    public void runFromScratch(ActionEvent actionEvent) {
    }
}
