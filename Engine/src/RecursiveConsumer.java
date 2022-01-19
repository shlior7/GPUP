import java.util.function.Consumer;

@FunctionalInterface
public interface RecursiveConsumer<T> extends Consumer<T> {
    default void accept(T t) {
        accept(this, t);
    }

    public void accept(RecursiveConsumer<T> runnable, T t);
}