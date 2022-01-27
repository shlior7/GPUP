package utils;

import javafx.scene.control.Alert;

import java.util.Iterator;
import java.util.function.BiConsumer;

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
}
