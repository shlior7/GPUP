package utils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;
import types.TaskInfo;

public class CheckBoxTableCellFactory<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {
//    BooleanProperty selected = new SimpleBooleanProperty();
//    CheckBoxTableCell<S, T> ctCell = new CheckBoxTableCell<>();

    public TableCell<S, T> call(TableColumn<S, T> param) {
        BooleanProperty selected = new SimpleBooleanProperty();
        CheckBoxTableCell<S, T> ctCell = new CheckBoxTableCell<>();
        ctCell.setSelectedStateCallback(index -> {
                    ((TaskInfo) param.getTableView().getItems().get(index)).getRegisteredProperty().bind(selected);
                    return selected;
                }
        );
        return ctCell;
    }
}