import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Task extends Runnable {
    String getName();

    void setTarget(Target target);

    void setFuncOnFinished(BiConsumer<Target, Task> onFinished);
}
