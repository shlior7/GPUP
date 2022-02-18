package utils;

import java.util.function.Consumer;

@FunctionalInterface
public interface RecursiveConsumer<T> extends Consumer<T> {
    default void accept(T t) {
        accept(this, t);
    }

    void accept(RecursiveConsumer<T> runnable, T t);
}