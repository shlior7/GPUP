package app.components.Task;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TaskComponent extends VBox {
    private TaskData taskData;
    @FXML
    private Text task_name;

    @FXML
    private Text task_createdBy;

    @FXML
    private Text task_type;

    @FXML
    private Text task_targets;

    @FXML
    private VBox task_leafs;

    @FXML
    private VBox task_middle;

    @FXML
    private VBox task_roots;

    @FXML
    private VBox task_independent;

    @FXML
    private Text task_creditPerTarget;

    @FXML
    private Text task_status;

    @FXML
    private Text task_workers;

    @FXML
    private Text task_registered;

    @FXML
    void initialize() {
        task_name.setText(taskData.name);
        task_createdBy.setText(taskData.createdBy.userName);
        task_type.setText(taskData.type);
        task_targets.setText(String.valueOf(taskData.targets));
        task_leafs.getChildren().addAll(Arrays.stream(taskData.targetsPerType.get("leaf")).map(Text::new).collect(Collectors.toList()));
        task_middle.getChildren().addAll(Arrays.stream(taskData.targetsPerType.get("middle")).map(Text::new).collect(Collectors.toList()));
        task_roots.getChildren().addAll(Arrays.stream(taskData.targetsPerType.get("root")).map(Text::new).collect(Collectors.toList()));
        task_independent.getChildren().addAll(Arrays.stream(taskData.targetsPerType.get("independent")).map(Text::new).collect(Collectors.toList()));
        task_creditPerTarget.setText(String.valueOf(taskData.creditPerTarget));
        task_status.setText(String.valueOf(taskData.status));
        task_workers.setText(String.valueOf(taskData.workers));
        task_registered.setText(taskData.registered ? "Yes" : "No");
    }


    public TaskComponent(TaskData taskData) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "task_component.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        this.taskData = taskData;

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

}

