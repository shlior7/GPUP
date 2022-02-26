/**
 * A table cell containing a button for adding a new person.
 */
package utils;

import com.google.gson.JsonObject;
import engine.TaskProcessor;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import types.Task;
import types.TaskInfo;
import types.TaskStatus;
import utils.http.HttpClientUtil;
import utils.http.SimpleCallBack;

import static utils.Constants.GSON_INSTANCE;

public class SignUpCell extends TableCell<TaskInfo, Boolean> {
    // a button for adding a new person.
    final CheckBox signUpCheckbox = new CheckBox();
    // pads and centers the add button in the cell.
    final StackPane paddedPane = new StackPane();
    // records the y pos of the last button press so that the add person dialog can be shown next to the cell.
    final TaskProcessor taskProcessor;

    /**
     * AddPersonCell constructor
     *
     * @param table the table to which a new person can be added.
     */
    public SignUpCell(final TableView<TaskInfo> table, TaskProcessor taskProcessor) {
        this.taskProcessor = taskProcessor;
        paddedPane.setPadding(new Insets(3));
        paddedPane.getChildren().add(signUpCheckbox);

        signUpCheckbox.setOnAction(actionEvent -> {
            TaskInfo task = table.getItems().get(getTableRow().getIndex());
            task.setRegistered(signUpCheckbox.isSelected());
            signUpCheckbox.selectedProperty().bindBidirectional(task.getRegisteredProperty());
            if (signUpCheckbox.isSelected()) {
                signToTasks(task.getTaskName(), true);
                signUpCheckbox.setDisable(true);
            }
        });


        signUpCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("changed checkbox newValue = " + newValue);
            if (!newValue) {
                TaskInfo task = table.getItems().get(getTableRow().getIndex());
                System.out.println("changed to false task = " + task.getTaskName() + " " + task.getRegistered());
                if (!task.getTaskStatus().equals(TaskStatus.FINISHED.toString())) {
                    signToTasks(task.getTaskName(), false);
                    signUpCheckbox.setDisable(false);
                } else {
                    signUpCheckbox.setDisable(true);
                }
            }
        });
    }

    private synchronized void signToTasks(String taskName, boolean signTo) {
        String finalUrl = HttpClientUtil.createUrl(Constants.TASK_SIGN
                , Utils.tuple(Constants.SIGNTO, String.valueOf(signTo))
                , Utils.tuple(Constants.TASKNAME, taskName));

        System.out.println("finalUrl " + finalUrl);

        HttpClientUtil.runAsync(finalUrl, new SimpleCallBack((taskJson) -> {
            System.out.println(taskJson);
            if (signTo) {
                JsonObject json = GSON_INSTANCE.fromJson(taskJson, JsonObject.class);
                System.out.println("taskJson = " + taskJson);
                Task task = Utils.getTaskFromJson(json);
                task.setCreditPerTarget(json.get("creditPerTarget").getAsInt());
                taskProcessor.pushTask(task);
            } else {
                taskProcessor.removeTask(taskName);
            }
        }));
    }

    /**
     * places an add button in the row only if the row is not empty.
     */
    @Override
    protected void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setGraphic(paddedPane);
        }
    }
}
