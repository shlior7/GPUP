import javafx.beans.property.SimpleStringProperty;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Task implements Runnable {
    protected Target targetToRunOn;
    protected Consumer<Target> onFinished;
    protected Consumer<String> outputText;

    public abstract Task copy();

    void setTarget(Target targetToRunOn) {
        this.targetToRunOn = targetToRunOn;
    }

    void setFuncOnFinished(Consumer<Target> onFinished) {
        this.onFinished = onFinished;
    }

    void setOutputText(Consumer<String> outputText) {
        this.outputText = outputText;
    }

    public abstract String getName();

}
