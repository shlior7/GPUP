/**
 * A table cell containing a button for adding a new person.
 */
package utils;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import types.TaskInfo;

public class ProgressBarCell extends TableCell<TaskInfo, Boolean> {
    // a button for adding a new person.
    final ProgressBar progressBar = new ProgressBar();
    // pads and centers the add button in the cell.
    final StackPane paddedPane = new StackPane();
    // records the y pos of the last button press so that the add person dialog can be shown next to the cell.

    /**
     * AddPersonCell constructor
     *
     * @param table the table to which a new person can be added.
     */
    public ProgressBarCell(final TableView<TaskInfo> table) {
        paddedPane.setPadding(new Insets(1));
        paddedPane.getChildren().add(progressBar);
        System.out.println("table = " + getTableRow().getIndex());
        if (table != null) {
            TaskInfo task = table.getItems().get(getTableRow().getIndex());
            task.progressProperty().addListener((observable, oldValue, newValue) -> {
//                progressBar.setProgress(Double.parseDouble(newValue));
            });
        }
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
