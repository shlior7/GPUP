package app.dashboard;

import TargetGraph.TargetGraph;
import TargetGraph.GraphParams;
import app.utils.Constants;
import app.utils.FileHandler;
import app.utils.http.HttpClientUtil;
import app.utils.http.SimpleCallBack;
import com.google.gson.JsonObject;
import graphApp.GraphPane;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import types.GraphInfo;
import types.TaskInfo;
import types.UserInfo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static app.utils.Constants.GSON_INSTANCE;

public class Dashboard extends Stage implements Initializable {
    GraphInfo[] graphInfos = new GraphInfo[0];
    Map<String, GraphPane> graphPanes = new HashMap<>();


    @FXML
    public TableView<UserInfo> UsersTable;
    @FXML
    public TableView<GraphInfo> graphTable;
    @FXML
    public TableView<TaskInfo> taskTable;
    @FXML
    public ComboBox<String> graphComboBox;
    @FXML
    public AnchorPane graphsRoot;

    public static Dashboard createDashboard() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Dashboard.class.getResource("admin_dashboard.fxml"));
        TabPane root = fxmlLoader.load();
        Dashboard dashboard = fxmlLoader.getController();
        dashboard.setScene(new Scene(root, 2048, 1800));
        dashboard.createGraphListener();
        dashboard.getUsers();
        dashboard.getGraphs();
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

        HttpClientUtil.runAsyncBody(finalUrl, RequestBody.create(MediaType.parse("text/xml"), file), new SimpleCallBack((message) -> {
            System.out.println(message);
            getGraphs();
        }));
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
            if (graphs != null && !Arrays.equals(graphs, graphInfos)) {
                setGraphTable(graphs);
                graphComboBox.setItems(FXCollections.observableList(Arrays.stream(graphs).map(GraphInfo::getGraphName).collect(Collectors.toList())));
            }
        }));
    }

    private void setGraphTable(GraphInfo[] graphs) {
        graphTable.getItems().clear();
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

    private void createGraphListener() {
        graphComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            GraphPane graphPane = graphPanes.getOrDefault(newValue, null);
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            FileChooser.ExtensionFilter xmlfilter = new FileChooser.ExtensionFilter(
                    "XML Files (*.xml)", "*.xml");
            fileChooser.getExtensionFilters().add(xmlfilter);
//            File file = fileChooser.showOpenDialog(null);
            FileHandler fileHandler = new FileHandler();
            try {
//                TargetGraph t = fileHandler.loadGPUPXMLFile(file);
                if (graphPane == null) {
                    getGraph(newValue, (graphJson) -> {
                        try {
                            TargetGraph graph = new TargetGraph(GSON_INSTANCE.fromJson(graphJson, GraphParams.class));
                            GraphPane gp = new GraphPane(graph, null);
                            graphPanes.put(newValue, gp);
                            setGraph(gp);
                        } catch (Exception ignored) {
                        }
                    });
                } else {
                    setGraph(graphPane);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    private void setGraph(GraphPane graphPane1) {
        graphsRoot.getChildren().clear();
        AnchorPane.setTopAnchor(graphPane1, 0.0);
        AnchorPane.setLeftAnchor(graphPane1, 0.0);
        AnchorPane.setRightAnchor(graphPane1, 0.0);
        AnchorPane.setBottomAnchor(graphPane1, 0.0);
        graphsRoot.getChildren().add(graphPane1);
        Platform.runLater(graphPane1::init);
    }

    public void getGraph(String graphName, Consumer<String> callBack) {
        AtomicReference<TargetGraph> graph = new AtomicReference<>();
        String finalUrl = HttpUrl
                .parse(Constants.GET_GRAPH)
                .newBuilder()
                .addQueryParameter(Constants.GRAPHNAME, graphName)
                .build()
                .toString();
        System.out.println("finalUrl " + finalUrl);

        HttpClientUtil.runAsync(finalUrl, new SimpleCallBack(callBack));
    }


}
