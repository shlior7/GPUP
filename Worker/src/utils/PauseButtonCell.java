/**
 * A table cell containing a button for adding a new person.
 */
package utils;

import engine.TaskProcessor;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import types.TaskInfo;

import java.util.function.BiConsumer;

public class PauseButtonCell extends ButtonCell {
    TableView<TaskInfo> table;

    /**
     * AddPersonCell constructor
     *
     * @param table the table to which a new person can be added.
     */
    public PauseButtonCell(final TableView<TaskInfo> table, TaskProcessor taskProcessor) {
        this.table = table;
        button.setOnAction(actionEvent -> {
            TaskInfo task = table.getItems().get(getTableRow().getIndex());
            boolean running = taskProcessor.togglePause(task.getTaskName());
            task.setPaused(!running);
            button.setText(running ? "Pause" : "Resume");
        });

    }

    /**
     * places an add button in the row only if the row is not empty.
     */
    @Override
    protected void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            TaskInfo task = table.getItems().get(getTableRow().getIndex());
            button.setText(task.isPaused() ? "Resume" : "Pause");
        }
    }
}
