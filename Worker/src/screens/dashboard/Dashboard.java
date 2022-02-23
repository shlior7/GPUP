package screens.dashboard;

import TargetGraph.TargetGraph;
import TargetGraph.Target;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import okhttp3.HttpUrl;
import types.*;
import utils.Constants;
import utils.http.HttpClientUtil;
import utils.http.SimpleCallBack;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import static utils.Constants.GSON_INSTANCE;


public class Dashboard extends Stage implements Initializable {

    private Worker worker;

    private int credits = 0;

    GraphInfo[] graphInfos = new GraphInfo[0];

    Map<String, TargetGraph> TargetGraphs = new HashMap<>();

    List<TargetInfo> TargetsInfo = new ArrayList<>();

    @FXML
    private TableView<TargetInfo> targetsTable;

    @FXML
    private Label numberOfCredits;

    @FXML
    private Label availableThreads;

    @FXML
    private TableColumn<?, ?> selectedListMissions;

    @FXML
    private ListView<String> missionsList;

    @FXML
    public TableView<UserInfo> usersTable;

    @FXML
    private TableView<GraphInfoWorker> taskTable;

    @FXML
    public TableView<GraphInfo> graphTable;


    public static Dashboard createDashboard(Worker worker) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Dashboard.class.getResource("worker_dashboard.fxml"));
        ScrollPane root = fxmlLoader.load();
        Dashboard dashboard = fxmlLoader.getController();
        dashboard.worker = worker;
        dashboard.availableThreads.setText(String.valueOf(worker.getThreads()));
        dashboard.numberOfCredits.setText(String.valueOf(dashboard.credits));
        dashboard.setScene(new Scene(root, 2048, 1800));
        dashboard.getUsers();
        return dashboard;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
            }
        }));
    }

    private void setGraphTable(GraphInfo[] graphs) {
        Platform.runLater(() -> {
            graphTable.getItems().clear();
            graphTable.getItems().addAll(graphs);
        });
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

    private void setUserTable(UserInfo[] users) {
        Platform.runLater(() -> {
            usersTable.getItems().clear();
            usersTable.getItems().addAll(users);
        });
    }

    public void setTargetsTable(ObservableList targetsInfo) {
        Platform.runLater(() -> {
            targetsTable.getItems().clear();
            targetsTable.getItems().addAll(targetsInfo);
        });
    }

    public synchronized void addTargetInfo(Task task, Target target){
        if(task.getWorkerOnIt().getName().equals(worker.getName())) {
            this.TargetsInfo.add(new TargetInfo(task, target));
            setTargetsTable(FXCollections.observableList(TargetsInfo));
        }
    }

    public synchronized void changeTargetInfo(Task task, Target target){
        if(task.getWorkerOnIt().getName().equals(worker.getName())) {
            TargetsInfo.removeIf(targetInfo -> targetInfo.getTargetName().equals(target.getName()) &&
                    targetInfo.getTaskName().equals(task.getTaskName()));
            this.TargetsInfo.add(new TargetInfo(task, target));
            setTargetsTable(FXCollections.observableList(TargetsInfo));
        }
    }
}