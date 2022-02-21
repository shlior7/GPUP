package types;

import TargetGraph.Target;

import java.util.function.Consumer;

public abstract class Task implements Runnable {
    protected int pricePerTarget;
    protected String taskName;
    protected Target targetToRunOn;
    protected Worker workerOnIt;
    protected Consumer<Target> onFinished;
    protected Consumer<String> outputText;

    public Task(String taskName) {
        this.taskName = taskName;
    }

    public abstract Task copy();

    public void setWorkerOnIt(Worker workerOnIt) {
        this.workerOnIt = workerOnIt;
    }

    public void setTarget(Target targetToRunOn) {
        this.targetToRunOn = targetToRunOn;
    }

    public void setFuncOnFinished(Consumer<Target> onFinished) {
        this.onFinished = onFinished;
    }

    public void setOutputText(Consumer<String> outputText) {
        this.outputText = outputText;
    }

    public abstract String getName();

    public String getTaskName() {
        return taskName;
    }

    public void setPricePerTarget(int pricePerTarget) {
        this.pricePerTarget = pricePerTarget;
    }
}
