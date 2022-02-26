package screens.dashboard;

import engine.TaskProcessor;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import types.*;
import utils.*;
import utils.http.HttpClientUtil;
import utils.http.SimpleCallBack;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

import static utils.Constants.GSON_INSTANCE;
import static utils.Utils.setAndAddToTable;


public class Dashboard extends Stage implements Initializable {
    private TaskProcessor taskProcessor;
    private Timer taskTimer;
    private Timer targetsTimer;
    private Timer userTimer;
    List<TargetInfo> TargetsInfo = new ArrayList<>();

    @FXML
    public TableColumn<TaskInfo, String> targetsColumn;
    @FXML
    public TableColumn<TaskInfo, Boolean> stopColumn;

    @FXML
    public TableColumn<TaskInfo, Boolean> pausedColumn;
    @FXML
    public TableColumn<TaskInfo, Double> progressColumn;

    @FXML
    public TableView<TaskInfo> myTasksTable;

    @FXML
    public Label threadsLabel;

    @FXML
    public Label nameLabel;

    @FXML
    public Label creditsLabel;

    @FXML
    private TableView<TargetInfo> targetsTable;

    @FXML
    private Label availableThreads;

    @FXML
    public TableView<UserInfo> usersTable;

    @FXML
    private TableView<TaskInfo> taskTable;

    @FXML
    public TableColumn<TaskInfo, Boolean> registerColumn;

    public static Dashboard createDashboard(Worker worker) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Dashboard.class.getResource("worker_dashboard.fxml"));
        Pane root = fxmlLoader.load();
        Dashboard dashboard = fxmlLoader.getController();
        dashboard.init(root, worker);
        return dashboard;
    }

    public void init(Pane root, Worker worker) {
        this.taskProcessor = new TaskProcessor(worker.getThreads(), targetsTable, myTasksTable);

        this.nameLabel.setText(worker.getName());
        this.creditsLabel.textProperty().bind(taskProcessor.getCreditsBinding());
        this.availableThreads.setText(taskProcessor.availableThreads().toString());
        this.availableThreads.textProperty().bind(taskProcessor.availableThreads().asString());

        this.setScene(new Scene(root, 2048, 1800));
        this.getUsers();
        this.initTimers();
        this.getTasks();
        this.setCellFactories();

    }

    public void setCellFactories() {
        registerColumn.setCellValueFactory(features -> new SimpleBooleanProperty(features.getValue() != null));
        registerColumn.setCellFactory(personBooleanTableColumn -> new SignUpCell(taskTable, taskProcessor));

        pausedColumn.setCellValueFactory(features -> new SimpleBooleanProperty(features.getValue() != null));
        pausedColumn.setCellFactory(personBooleanTableColumn -> new PauseButtonCell(myTasksTable, taskProcessor));

        stopColumn.setCellValueFactory(features -> new SimpleBooleanProperty(features.getValue() != null));
        stopColumn.setCellFactory(personBooleanTableColumn -> new StopButtonCell(myTasksTable, taskProcessor));

        progressColumn.setCellValueFactory(new PropertyValueFactory<>("progress"));
        progressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());

        targetsColumn.setCellValueFactory(new PropertyValueFactory<>("targetsProcessed"));
    }

    public void initTimers() {
        taskTimer = new Timer();
        taskTimer.schedule(new TimerTask() {
            public void run() {
                getTasks();
            }
        }, 5 * 1000, 2 * 1000);

        userTimer = new Timer();
        userTimer.schedule(new TimerTask() {
            public void run() {
                getUsers();
            }
        }, 1000, 2000);

        targetsTimer = new Timer();
        targetsTimer.schedule(new TimerTask() {
            public void run() {
                taskProcessor.getMoreTargets();
            }
        }, 0, 1000);
    }

    public void getUsers() {
        String url = HttpClientUtil.createUrl(Constants.GET_USERS_ALL);

        HttpClientUtil.runAsync(url, new SimpleCallBack((graphJson) -> {
            UserInfo[] users = GSON_INSTANCE.fromJson(graphJson, UserInfo[].class);
            if (users != null) setUserTable(users);
        }));
    }


    private void setUserTable(UserInfo[] users) {
        Platform.runLater(() -> {
            usersTable.getItems().clear();
            usersTable.getItems().addAll(users);
        });
    }

    private void getTasks() {
        String url = HttpClientUtil.createUrl(Constants.GET_TASK_ALL);

        HttpClientUtil.runAsync(url, new SimpleCallBack((tasksJson) -> {
            try {
                System.out.println("tasksJson " + tasksJson);
                TaskInfo[] tasks = GSON_INSTANCE.newBuilder().excludeFieldsWithModifiers(Modifier.PRIVATE).create().fromJson(tasksJson, TaskInfo[].class);

                System.out.println(Arrays.toString(tasks));

                List<TaskInfo> myTasks = new ArrayList<>();
                for (TaskInfo task : tasks) {
                    if (task.registered.get()) {
                        myTasks.add(task);
                    }
                    if (task.getTaskStatus().equals(TaskStatus.FINISHED.toString())) {
                        taskProcessor.removeTask(task.getTaskName());
                    }
                }

                setTaskTable(tasks);
                taskProcessor.setMyTasksTable(myTasks);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }


    private void setTaskTable(TaskInfo[] tasks) {
        setAndAddToTable(tasks, taskTable
                , (t1, t2) -> t1.setTaskStatus(t2.getTaskStatus())
                , (t1, t2) -> t1.setRegistered(t2.getRegistered())
                , (t1, t2) -> t1.setWorkers(t2.getWorkers()));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}