package task;

import TargetGraph.Target;
import types.Worker;

import java.util.function.Consumer;

public abstract class Task implements Runnable {
    protected Target targetToRunOn;
    protected Worker workerOnIt;
    protected Consumer<Target> onFinished;
    protected Consumer<String> outputText;

    public abstract Task copy();

    public void setWorkerOnIt(Worker workerOnIt) {
        this.workerOnIt = workerOnIt;
    }

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
