package types;


import TargetGraph.Type;
import javafx.beans.property.*;
import lombok.ToString;

@ToString
public class TaskInfo extends TableItem {
    public String taskName;
    public String createdBy;
    public String graphName;
    public String type;
    public String targets;
    public String leaves;
    public String middles;
    public String independents;
    public String roots;
    public String creditPerTarget;

    public final BooleanProperty registered;
    public StringProperty taskStatus;
    public StringProperty creditsFromTask;
    public StringProperty workers;
    public StringProperty targetsProcessed;
    public DoubleProperty progress;

    public TaskInfo() {
        super("");
        this.registered = new SimpleBooleanProperty(false);
    }


    public TaskInfo(TaskData taskData, boolean registered, double progress) {
        this(taskData, registered, progress, 0);
    }

    public TaskInfo(TaskData taskData, boolean registered, double progress, int creditsFromTask) {
        super(taskData.getTask().getTaskName());
        this.taskName = taskData.getTask().getTaskName();
        this.createdBy = taskData.getCreatedBy().getName();
        this.graphName = taskData.getTargetGraph().getGraphsName();
        this.type = taskData.getTask().getClassName();
        this.targets = String.valueOf(taskData.getTargetGraph().totalSize());
        this.leaves = ifNullZero(String.valueOf(taskData.getTargetGraph().getTypesStatistics().get(Type.leaf)));
        this.middles = ifNullZero(String.valueOf(taskData.getTargetGraph().getTypesStatistics().get(Type.middle)));
        this.independents = ifNullZero(String.valueOf(taskData.getTargetGraph().getTypesStatistics().get(Type.independent)));
        this.roots = ifNullZero(String.valueOf(taskData.getTargetGraph().getTypesStatistics().get(Type.root)));
        this.creditPerTarget = String.valueOf(taskData.getTargetGraph().getPrices().get(TaskType.valueOf(taskData.getTask().getClassName())));

        setTaskStatus(taskData.getStatus().toString());
        setWorkers(String.valueOf(taskData.getWorkerListMap().keySet().size()));
        setProgress(progress);
        setCreditsFromTask(String.valueOf(creditsFromTask));
        setTargetsProcessed("0");

        this.registered = new SimpleBooleanProperty(registered);
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTargetsProcessed() {
        return targetsProcessedProperty().get();
    }

    public void setTargetsProcessed(String value) {
        targetsProcessedProperty().set(value);
    }

    private StringProperty targetsProcessedProperty() {
        if (targetsProcessed == null) targetsProcessed = new SimpleStringProperty(this, "targetsProcessed");
        return targetsProcessed;
    }

    public String getCreditsFromTask() {
        return creditsFromTaskProperty().get();
    }

    public void setCreditsFromTask(String valueOf) {
        creditsFromTaskProperty().set(valueOf);
    }

    public StringProperty creditsFromTaskProperty() {
        if (creditsFromTask == null) creditsFromTask = new SimpleStringProperty(this, "creditsFromTask");
        return creditsFromTask;
    }

    public Double getProgress() {
        return progressProperty().get();
    }

    public void setProgress(double valueOf) {
        progressProperty().set(valueOf);
    }

    public StringProperty workersProperty() {
        if (workers == null) workers = new SimpleStringProperty(this, "workers");
        return workers;
    }

    public DoubleProperty progressProperty() {
        if (progress == null) progress = new SimpleDoubleProperty(this, "progress");
        return progress;
    }

    public StringProperty taskStatusProperty() {
        if (taskStatus == null) taskStatus = new SimpleStringProperty(this, "taskStatus");
        return taskStatus;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public String getGraphName() {
        return this.graphName;
    }

    public String getType() {
        return this.type;
    }

    public String getTargets() {
        return this.targets;
    }

    public String getLeaves() {
        return this.leaves;
    }

    public String getMiddles() {
        return this.middles;
    }

    public String getIndependents() {
        return this.independents;
    }

    public String ifNullZero(String string) {
        if (string == null)
            return "o";
        return string;
    }

    public String getRoots() {
        return this.roots;
    }

    public String getCreditPerTarget() {
        return this.creditPerTarget;
    }

    public String getTaskStatus() {
        return taskStatusProperty().get();
    }

    public String getWorkers() {
        return this.workersProperty().get();
    }

    public boolean getRegistered() {
        return this.registered.get();
    }

    public BooleanProperty getRegisteredProperty() {
        return this.registered;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTargets(String targets) {
        this.targets = targets;
    }

    public void setLeaves(String leaves) {
        this.leaves = leaves;
    }

    public void setMiddles(String middles) {
        this.middles = middles;
    }

    public void setIndependents(String independents) {
        this.independents = independents;
    }

    public void setRoots(String roots) {
        this.roots = roots;
    }

    public void setCreditPerTarget(String creditPerTarget) {
        this.creditPerTarget = creditPerTarget;
    }

    public void setTaskStatus(String taskStatus) {
        taskStatusProperty().set(taskStatus);
    }

    public void setWorkers(String workers) {
        workersProperty().set(workers);
    }

    public void setRegistered(boolean registered) {
        this.registered.setValue(registered);
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof TaskInfo)) return false;
        final TaskInfo other = (TaskInfo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$taskName = this.getTaskName();
        final Object other$taskName = other.getTaskName();
        if (this$taskName == null ? other$taskName != null : !this$taskName.equals(other$taskName)) return false;
        final Object this$createdBy = this.getCreatedBy();
        final Object other$createdBy = other.getCreatedBy();
        if (this$createdBy == null ? other$createdBy != null : !this$createdBy.equals(other$createdBy)) return false;
        final Object this$graphName = this.getGraphName();
        final Object other$graphName = other.getGraphName();
        if (this$graphName == null ? other$graphName != null : !this$graphName.equals(other$graphName)) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        final Object this$targets = this.getTargets();
        final Object other$targets = other.getTargets();
        if (this$targets == null ? other$targets != null : !this$targets.equals(other$targets)) return false;
        final Object this$leaves = this.getLeaves();
        final Object other$leaves = other.getLeaves();
        if (this$leaves == null ? other$leaves != null : !this$leaves.equals(other$leaves)) return false;
        final Object this$middles = this.getMiddles();
        final Object other$middles = other.getMiddles();
        if (this$middles == null ? other$middles != null : !this$middles.equals(other$middles)) return false;
        final Object this$independents = this.getIndependents();
        final Object other$independents = other.getIndependents();
        if (this$independents == null ? other$independents != null : !this$independents.equals(other$independents))
            return false;
        final Object this$roots = this.getRoots();
        final Object other$roots = other.getRoots();
        if (this$roots == null ? other$roots != null : !this$roots.equals(other$roots)) return false;
        final Object this$creditPerTarget = this.getCreditPerTarget();
        final Object other$creditPerTarget = other.getCreditPerTarget();
        if (this$creditPerTarget == null ? other$creditPerTarget != null : !this$creditPerTarget.equals(other$creditPerTarget))
            return false;
        final Object this$taskStatus = this.getTaskStatus();
        final Object other$taskStatus = other.getTaskStatus();
        if (this$taskStatus == null ? other$taskStatus != null : !this$taskStatus.equals(other$taskStatus))
            return false;
        final Object this$workers = this.getWorkers();
        final Object other$workers = other.getWorkers();
        if (this$workers == null ? other$workers != null : !this$workers.equals(other$workers)) return false;

        final BooleanProperty this$registered = this.getRegisteredProperty();
        final BooleanProperty other$registered = other.getRegisteredProperty();
        if (this$registered == null ? other$registered != null : !this$registered.getValue().equals(other$registered.getValue()))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TaskInfo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $taskName = this.getTaskName();
        result = result * PRIME + ($taskName == null ? 43 : $taskName.hashCode());
        final Object $createdBy = this.getCreatedBy();
        result = result * PRIME + ($createdBy == null ? 43 : $createdBy.hashCode());
        final Object $graphName = this.getGraphName();
        result = result * PRIME + ($graphName == null ? 43 : $graphName.hashCode());
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final Object $targets = this.getTargets();
        result = result * PRIME + ($targets == null ? 43 : $targets.hashCode());
        final Object $leaves = this.getLeaves();
        result = result * PRIME + ($leaves == null ? 43 : $leaves.hashCode());
        final Object $middles = this.getMiddles();
        result = result * PRIME + ($middles == null ? 43 : $middles.hashCode());
        final Object $independents = this.getIndependents();
        result = result * PRIME + ($independents == null ? 43 : $independents.hashCode());
        final Object $roots = this.getRoots();
        result = result * PRIME + ($roots == null ? 43 : $roots.hashCode());
        final Object $creditPerTarget = this.getCreditPerTarget();
        result = result * PRIME + ($creditPerTarget == null ? 43 : $creditPerTarget.hashCode());
        final Object $taskStatus = this.getTaskStatus();
        result = result * PRIME + ($taskStatus == null ? 43 : $taskStatus.hashCode());
        final Object $workers = this.getWorkers();
        result = result * PRIME + ($workers == null ? 43 : $workers.hashCode());
        final Object $registered = this.getRegistered();
        result = result * PRIME + ($registered == null ? 43 : $registered.hashCode());
        return result;
    }
}
