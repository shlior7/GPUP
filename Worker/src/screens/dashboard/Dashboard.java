package screens.dashboard;

import TargetGraph.TargetGraph;
import TargetGraph.Target;
import com.google.gson.reflect.TypeToken;
import engine.TaskProcessor;
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
import utils.Utils;
import utils.http.HttpClientUtil;
import utils.http.SimpleCallBack;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static utils.Constants.GSON_INSTANCE;


public class Dashboard extends Stage implements Initializable {
    private TaskProcessor taskProcessor;
    private Worker worker;
    private TaskInfo[] taskInfos;
    private int credits = 0;

    private Timer taskTimer;
    private Timer targetsTimer;
    private Timer userTimer;


    List<TargetInfo> TargetsInfo = new ArrayList<>();

    @FXML
    private Label numberOfCredits;

    @FXML
    private TableView<TargetInfo> targetsTable;

    @FXML
    private Label availableThreads;

    @FXML
    private ListView<String> myTasks;

    @FXML
    public TableView<UserInfo> usersTable;

    @FXML
    private TableView<TaskInfo> taskTable;

    @FXML
    public TableColumn<TaskInfo, Boolean> registerColumn;

    public static Dashboard createDashboard(Worker worker) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Dashboard.class.getResource("worker_dashboard.fxml"));
        ScrollPane root = fxmlLoader.load();
        Dashboard dashboard = fxmlLoader.getController();
        dashboard.init(root, worker);
        return dashboard;
    }

    public void init(ScrollPane root, Worker worker) {
        this.worker = worker;
        this.availableThreads.textProperty().bind(taskProcessor.availableThreads().asString());
        this.numberOfCredits.setText(String.valueOf(this.credits));
        this.setScene(new Scene(root, 2048, 1800));
        this.getUsers();
        this.initTimers();
        this.getTasks();
        taskProcessor = new TaskProcessor(worker.getThreads());
    }

    public void initTimers() {
        taskTimer = new Timer();
        taskTimer.schedule(new TimerTask() {
            public void run() {
                getTasks();
            }
        }, 10 * 1000, 5 * 60 * 1000);

        userTimer = new Timer();
        userTimer.schedule(new TimerTask() {
            public void run() {
                getUsers();
            }
        }, 0, 5 * 60 * 1000);

        targetsTimer = new Timer();
        targetsTimer.schedule(new TimerTask() {
            public void run() {
                taskProcessor.getMoreTargets();
            }
        }, 0, 1000);
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

    private void getTasks() {
        String finalUrl = HttpUrl
                .parse(Constants.GET_TASK_ALL)
                .newBuilder()
                .build()
                .toString();
        System.out.println("finalUrl " + finalUrl);

        Platform.runLater(() -> {
            HttpClientUtil.runAsync(finalUrl, new SimpleCallBack((tasksJson) -> {
                System.out.println(tasksJson);
                TaskInfo[] tasks = GSON_INSTANCE.newBuilder().excludeFieldsWithModifiers(Modifier.PRIVATE).create().fromJson(tasksJson, TaskInfo[].class);
                System.out.println(Arrays.toString(tasks));
                if (tasks != null && !Arrays.equals(tasks, taskInfos)) setTaskTable(tasks);
            }));
        });
    }

    private void setTaskTable(TaskInfo[] tasks) {
        Platform.runLater(() -> {
            taskInfos = tasks;
            Arrays.stream(tasks).forEach(task -> {
                task.getRegisteredProperty().addListener(((observable, oldValue, newValue) -> {
                    ////Worker pressed on checkbox
                    System.out.println("task = " + task.getTaskName() + " old " + oldValue + " new " + newValue);
                    signToTasks(task.getTaskName(), newValue);
                }));
            });
            taskTable.getItems().clear();
            taskTable.getItems().addAll(tasks);
        });
    }

    private synchronized void signToTasks(String taskName, boolean signTo) {
        String finalUrl = HttpUrl
                .parse(Constants.TASK_SIGN)
                .newBuilder()
                .addQueryParameter(Constants.SIGNTO, String.valueOf(signTo))
                .addQueryParameter(Constants.TASKNAME, taskName)
                .build()
                .toString();
        System.out.println("finalUrl " + finalUrl);
        
        Platform.runLater(() -> {
            HttpClientUtil.runAsync(finalUrl, new SimpleCallBack((tasksJson) -> {
                System.out.println(tasksJson);
                if (signTo) {
                    taskProcessor.pushTask(Utils.getTaskFromJson(tasksJson));
                } else {
                    taskProcessor.removeTask(taskName);
                }
            }));
        });
    }

    public void setTargetsTable(ObservableList<TargetInfo> targetsInfo) {
        Platform.runLater(() -> {
            targetsTable.getItems().clear();
            targetsTable.getItems().addAll(targetsInfo);
        });
    }

    public synchronized void addTargetInfo(Task task) {
        this.TargetsInfo.add(new TargetInfo(task));
        setTargetsTable(FXCollections.observableList(TargetsInfo));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}