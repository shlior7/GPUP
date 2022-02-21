package types;

import TargetGraph.Target;

import java.util.function.Consumer;

public abstract class Task implements Runnable {
    protected int pricePerTarget;
    protected String taskName;
    protected Target targetToRunOn;
    protected Worker workerOnIt;
    protected Consumer<String> outputText;
    protected String type;

    public Task(String taskName, Class<? extends Task> type) {
        this.taskName = taskName;
        this.type = type.getName();
    }

    public abstract Task copy();

    public void setWorkerOnIt(Worker workerOnIt) {
        this.workerOnIt = workerOnIt;
    }

    public void setTarget(Target targetToRunOn) {
        this.targetToRunOn = targetToRunOn;
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

    public Target getTarget() {
        return targetToRunOn;
    }
}
