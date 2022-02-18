package engine.task;

import TargetGraph.Target;

import java.util.function.Consumer;

public abstract class Task implements Runnable {
    protected Target targetToRunOn;
    protected String workerOnIt;
    protected Consumer<Target> onFinished;
    protected Consumer<String> outputText;

    public abstract Task copy();

    void setWorkerOnIt(String workerOnIt) {
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
