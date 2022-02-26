/**
 * A table cell containing a button for adding a new person.
 */
package utils;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import types.TaskInfo;

public class ButtonCell extends TableCell<TaskInfo, Boolean> {
    // a button for adding a new person.
    final Button button = new Button();
    // pads and centers the add button in the cell.
    final StackPane paddedPane = new StackPane();
    // records the y pos of the last button press so that the add person dialog can be shown next to the cell.

    public ButtonCell() {
        paddedPane.setPadding(new Insets(1));
        paddedPane.getChildren().add(button);

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
