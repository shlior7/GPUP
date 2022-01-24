import java.util.function.Consumer;

public abstract class Task implements Runnable {
    protected Target targetToRunOn;
    protected Consumer<Target> onFinished;

    public abstract Task copy();

    void setTarget(Target targetToRunOn) {
        this.targetToRunOn = targetToRunOn;
    }

    void setFuncOnFinished(Consumer<Target> onFinished) {
        this.onFinished = onFinished;
    }

    public abstract String getName();
}
