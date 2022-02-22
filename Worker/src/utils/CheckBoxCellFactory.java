package utils;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;
import types.TaskInfo;


public class CheckBoxCellFactory implements Callback<TableColumn<TaskInfo, Boolean>, TableCell<TaskInfo, Boolean>> {
    public TableCell<TaskInfo, Boolean> call(TableColumn<TaskInfo, Boolean> param) {
        CheckBoxTableCell<TaskInfo, Boolean> checkBox = new CheckBoxTableCell<>();
        checkBox.setSelectedStateCallback(index -> {
            TaskInfo taskInfo = param.getTableView().getItems().get(index);
            ObservableValue<Boolean> itemBoolean = taskInfo.getRegisteredProperty();

            itemBoolean.addListener(change -> {
                if(taskInfo.getRegistered()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warning");
                    alert.show();
                }
            });
            return itemBoolean;
        });
        return new CheckBoxTableCell<>();
    }
}
