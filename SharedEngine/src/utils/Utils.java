package utils;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import types.TableItem;
import types.Task;
import types.Tuple;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static utils.Constants.GSON_INSTANCE;

public class Utils {
    public static Object getIfNullDefault(Object maybeNull, Object defaultObj) {
        return maybeNull != null ? maybeNull : defaultObj;
    }

    public static <T> void tupleIterator(Iterable<T> iterable, BiConsumer<T, T> consumer) {
        Iterator<T> it = iterable.iterator();
        if (!it.hasNext()) return;
        T first = it.next();

        while (it.hasNext()) {
            T next = it.next();
            consumer.accept(first, next);
            first = next;
        }
    }

    public static void alertWarning(String warning) {
        Alert information = new Alert(Alert.AlertType.WARNING);
        information.setTitle("Warning");
        information.setContentText(warning);
        information.showAndWait();
    }

    public static Task getTaskFromJson(JsonObject json) {
        Task task;
        try {
            task = GSON_INSTANCE.fromJson(json, (Class<? extends Task>) Class.forName(json.get("classType").getAsString()));
        } catch (ClassNotFoundException e) {
            task = null;
        }
        return task;
    }

    public static <X, Y> Tuple<X, Y> tuple(X x, Y y) {
        return new Tuple<>(x, y);
    }

    @SafeVarargs
    public static <V extends TableItem> void setAndAddToTable(V[] items, TableView<V> table, BiConsumer<V, V>... setFieldFunctions) {
        Platform.runLater(() -> {
            for (V item : items) {
                checkAndSetItem(table, item, setFieldFunctions);
            }
        });
    }

    @SafeVarargs
    public static <V extends TableItem> void setAndAddToTable(Collection<V> items, TableView<V> table, BiConsumer<V, V>... setFieldFunctions) {
        Platform.runLater(() -> {
            for (V item : items) {
                checkAndSetItem(table, item, setFieldFunctions);
            }
        });
    }

    @SafeVarargs
    public static <V extends TableItem> void setAddRemoveFromTable(V[] items, TableView<V> table, BiConsumer<V, V>... setFieldFunctions) {
        Collection<V> itemsCollection = Arrays.asList(items);
        setAddRemoveFromTable(itemsCollection, table, setFieldFunctions);
    }

    @SafeVarargs
    public static <V extends TableItem> void setAddRemoveFromTable(Collection<V> items, TableView<V> table, BiConsumer<V, V>... setFieldFunctions) {
        Platform.runLater(() -> {
            for (V item : items) {
                checkAndSetItem(table, item, setFieldFunctions);
            }
        });
        table.getItems().removeIf(tableItem -> items.stream().noneMatch(item -> tableItem.getId().equals(item.getId())));
    }

    private static <V extends TableItem> void checkAndSetItem(TableView<V> table, V item, BiConsumer<V, V>[] setFieldFunctions) {
        boolean found = false;
        for (V tableItem : table.getItems()) {
            if (item.getId().equals(tableItem.getId())) {
                found = true;
                for (BiConsumer<V, V> setField : setFieldFunctions)
                    setField.accept(tableItem, item);
            }
        }
        if (!found) {
            table.getItems().add(item);
        }
    }

    public static <V> String getStringValueOrZero(V string) {
        String res = String.valueOf(string);
        if (Objects.equals(res, "null") || res == null)
            return "0";
        return res;
    }

    public static String ifNullZero(String string) {
        if (Objects.equals(string, "null") || string == null)
            return "0";
        return string;
    }
}
