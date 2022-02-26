/**
 * A table cell containing a button for adding a new person.
 */
package utils;

import engine.TaskProcessor;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableView;
import types.TaskInfo;

public class StopButtonCell extends ButtonCell {
    String text = "Stop";

    /**
     * AddPersonCell constructor
     *
     * @param table the table to which a new person can be added.
     */
    public StopButtonCell(final TableView<TaskInfo> table, TaskProcessor taskProcessor) {
        button.setText(text);
        button.setOnAction(actionEvent -> {
            TaskInfo task = table.getItems().get(getTableRow().getIndex());
            task.setRegistered(false);
            taskProcessor.signToTasks(task.getTaskName(), false);
            table.getItems().removeIf(tableTask -> tableTask.getTaskName().equals(task.getTaskName()));
            taskProcessor.pause(task.getTaskName());
        });
    }
}
