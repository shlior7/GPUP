package types;

import java.io.IOException;
import java.util.Objects;

@FunctionalInterface
public interface TriConsumerE<T, U, V> {
    void accept(T t, U u, V v) throws IOException;

    default TriConsumerE<T, U, V> andThen(TriConsumerE<? super T, ? super U, ? super V> after) {
        Objects.requireNonNull(after);
        return (a, b, c) -> {
            accept(a, b, c);
            after.accept(a, b, c);
        };
    }
}
