/**
 * A table cell containing a button for adding a new person.
 */
package utils;

import com.google.gson.JsonObject;
import engine.TaskProcessor;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import types.Task;
import types.TaskInfo;
import utils.http.HttpClientUtil;
import utils.http.SimpleCallBack;

import java.util.function.BiConsumer;

import static utils.Constants.GSON_INSTANCE;

public class ButtonCell extends TableCell<TaskInfo, Boolean> {
    // a button for adding a new person.
    final Button button = new Button();
    // pads and centers the add button in the cell.
    final StackPane paddedPane = new StackPane();
    // records the y pos of the last button press so that the add person dialog can be shown next to the cell.

    /**
     * AddPersonCell constructor
     *
     * @param table the table to which a new person can be added.
     */
    public ButtonCell(String text, final TableView<TaskInfo> table, BiConsumer<Button, String> whenPressed) {
        button.setText(text);
        paddedPane.setPadding(new Insets(1));
        paddedPane.getChildren().add(button);
        button.setOnAction(actionEvent -> {
            TaskInfo task = table.getItems().get(getTableRow().getIndex());
            whenPressed.accept(button, task.getTaskName());
        });
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
