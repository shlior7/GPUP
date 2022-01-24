import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import org.controlsfx.control.CheckComboBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class TargetsCheckComboBox<V> extends CheckComboBox<V> {
    public TargetsCheckComboBox(List<V> items, Consumer<V> onAdd, Consumer<V> onRemove) {
        super(FXCollections.observableList(items));
        Set<V> prevAdded = new HashSet<>();
        Set<V> prevRemoved = new HashSet<>();
        getCheckModel().getCheckedItems().addListener((ListChangeListener<V>) change -> {
            change.next();
            Set<V> addedSet = new HashSet<>(change.getAddedSubList());
            if (!addedSet.equals(prevAdded)) {
                change.getAddedSubList().forEach(onAdd);
                prevAdded.clear();
                prevAdded.addAll(addedSet);
            }

            Set<V> removedSet = new HashSet<>(change.getRemoved());
            if (!removedSet.equals(prevRemoved)) {
                change.getRemoved().forEach(onRemove);
                prevRemoved.clear();
                prevRemoved.addAll(removedSet);
            }
        });
    }


}
