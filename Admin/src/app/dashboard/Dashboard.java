package app.dashboard;

import TargetGraph.TargetGraph;
import TargetGraph.GraphParams;
import app.components.TaskControllerAdmin;
import app.components.taskSettingsManager;
import graphApp.GraphPane;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
import utils.Constants;
import utils.http.HttpClientUtil;
import utils.http.SimpleCallBack;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static utils.Constants.GSON_INSTANCE;
import static utils.Utils.setAddRemoveFromTable;
import static utils.Utils.setAndAddToTable;

public class Dashboard extends Stage implements Initializable {
    GraphInfo[] graphInfos = new GraphInfo[0];
    Map<String, GraphPane> graphPanes = new HashMap<>();
    Timer taskTimer;
    Timer userTimer;

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
        dashboard.getTasks();
        dashboard.initTimers();
        return dashboard;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void initTimers() {
        taskTimer = new Timer();

        taskTimer.schedule(new TimerTask() {
            public void run() {
                getTasks();
            }
        }, 0, 10 * 1000);

        userTimer = new Timer();
        userTimer.schedule(new TimerTask() {
            public void run() {
                getUsers();
            }
        }, 0, 10 * 1000);
    }

    public void loadXml(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        FileChooser.ExtensionFilter xmlfilter = new FileChooser.ExtensionFilter(
                "XML Files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(xmlfilter);
        File file = fileChooser.showOpenDialog(null);
        if (file == null)
            return;
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
        setAndAddToTable(graphs, graphTable);
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
        Platform.runLater(() -> {
            UsersTable.getItems().clear();
            UsersTable.getItems().addAll(users);
        });
    }

    private void createGraphListener() {
        graphComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            GraphPane graphPane = graphPanes.getOrDefault(newValue, null);
            try {
                if (graphPane == null) {
                    getGraph(newValue, (graphJson) -> {
                        try {
                            TargetGraph graph = new TargetGraph(GSON_INSTANCE.fromJson(graphJson, GraphParams.class));
                            GraphPane gPane = new GraphPane(graph);
                            TaskControllerAdmin taskController = new TaskControllerAdmin(gPane);
                            gPane.sideController.addTaskControllerAction(taskController);
                            AnchorPane.setTopAnchor(gPane, 0.0);
                            AnchorPane.setLeftAnchor(gPane, 0.0);
                            AnchorPane.setRightAnchor(gPane, 0.0);
                            AnchorPane.setBottomAnchor(gPane, 0.0);
                            graphPanes.put(newValue, gPane);
                            setGraph(gPane);
                            Platform.runLater(gPane::init);
                        } catch (Exception e) {
                            e.printStackTrace();
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

    private void setGraph(GraphPane graphPane) {
        graphsRoot.getChildren().clear();
        graphsRoot.getChildren().add(graphPane);
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

    private void getTasks() {
        String finalUrl = HttpUrl
                .parse(Constants.GET_TASK_ALL)
                .newBuilder()
                .build()
                .toString();
        System.out.println("finalUrl " + finalUrl);

        HttpClientUtil.runAsync(finalUrl, new SimpleCallBack((tasksJson) -> {
            System.out.println(tasksJson);
            TaskInfo[] tasks = GSON_INSTANCE.fromJson(tasksJson, TaskInfo[].class);
            System.out.println(Arrays.toString(tasks));
            if (tasks != null) setTaskTable(tasks);
        }));
    }

    private void setTaskTable(TaskInfo[] tasks) {
        setAddRemoveFromTable(tasks, taskTable
                , (t1, t2) -> t1.setTaskStatus(t2.getTaskStatus())
                , (t1, t2) -> t1.setCreditPerTarget(String.valueOf(Integer.parseInt(t2.getCreditPerTarget()) * Integer.parseInt(t2.getTargets())))
                , (t1, t2) -> t1.setWorkers(t2.getWorkers()));
    }

    public void OnDashboardTabSelectionChanged(Event event) {
        getTasks();
    }

    public void open(ActionEvent actionEvent) {

    }
}
