package types;

import TargetGraph.Target;

import java.util.function.Consumer;

public abstract class Task implements Runnable {
    protected int creditPerTarget;
    protected String taskName;
    protected Target targetToRunOn;
    protected Consumer<String> outputText;
    protected Consumer<Target> onFinished;
    protected String classType;
    protected String className;

    public Task(String taskName, Class<? extends Task> type) {
        this.taskName = taskName;
        this.classType = type.getName();
        this.className = type.getSimpleName();
    }

    public abstract Task copy();

    public void setTarget(Target targetToRunOn) {
        this.targetToRunOn = targetToRunOn;
    }

    public int getCreditPerTarget() {
        return creditPerTarget;
    }

    public String getClassType() {
        return classType;
    }

    public void setOutputText(Consumer<String> outputText) {
        this.outputText = outputText;
    }

    public String getClassName() {
        return className;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setCreditPerTarget(int creditPerTarget) {
        this.creditPerTarget = creditPerTarget;
    }

    public Target getTarget() {
        return targetToRunOn;
    }

    public void setFuncOnFinished(Consumer<Target> onFuncFinished) {
        this.onFinished = onFuncFinished;
    }
}
