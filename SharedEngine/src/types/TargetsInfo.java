package types;

import TargetGraph.TargetGraph;
import TargetGraph.Type;
import lombok.ToString;

import java.util.Map;

@ToString
public class TargetsInfo {
    private final String taskName;
    private final String targetName;
    private String taskType;
    private String totalPrice;
    private String targetStatus;
    private String logs;


    public TargetsInfo(Task task,Target target) {
        this.taskName = task.getName();
        this.targetName = target.toString();

        this.priceForTask = "";
        this.pricePerTarget = "";
        this.taskName = "";
        this.taskStatus = "";
        this.createdBy = graph.getCreatedBy().getName();
        this.totalTargets = String.valueOf(graph.totalSize());
        Map<Type, Integer> typeIntegerMap = graph.getTypesStatistics();
        this.leaves = String.valueOf(typeIntegerMap.get(Type.leaf));
        this.independents = String.valueOf(typeIntegerMap.get(Type.independent));
        this.middles = String.valueOf(typeIntegerMap.get(Type.middle));
        this.roots = String.valueOf(typeIntegerMap.get(Type.root));
    }

    public String getPriceForTask() {
        return priceForTask;
    }

    public String getPricePerTarget() {
        return pricePerTarget;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public String getWorkersOnTask() {
        return workersOnTask;
    }

    public synchronized void addWorker() {
        workersOnTask = String.valueOf(Integer.parseInt(this.workersOnTask)+1);
    }

    public synchronized void decreaseWorker(){
        workersOnTask = String.valueOf(Integer.parseInt(this.workersOnTask)-1);
    }

    public String getGraphName() {
        return this.graphName;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public String getTotalTargets() {
        return this.totalTargets;
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

    public String getRoots() {
        return this.roots;
    }
}
