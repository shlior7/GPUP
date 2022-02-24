package utils;

import com.google.gson.JsonObject;
import javafx.scene.control.Alert;
import types.Task;
import types.Tuple;

import java.util.Iterator;
import java.util.function.BiConsumer;

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

    public static Task getTaskFromJson(String taskJson) {
        Task task;
        try {
            JsonObject json = GSON_INSTANCE.fromJson(taskJson, JsonObject.class);
            task = GSON_INSTANCE.fromJson(taskJson, (Class<? extends Task>) Class.forName(json.get("type").getAsString()));
        } catch (ClassNotFoundException e) {
            task = null;
        }
        return task;
    }

    public static <X, Y> Tuple<X, Y> tuple(X x, Y y) {
        return new Tuple<>(x, y);
    }
}
