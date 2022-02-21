package types;


import TargetGraph.Type;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class TaskInfo {
    public String name;
    public String createdBy;
    public String type;
    public String targets;
    public String leaves;
    public String middles;
    public String independents;
    public String roots;
    public String creditPerTarget;
    public String status;
    public String workers;
    public String registered;

    public TaskInfo(TaskData taskData) {
        this.name = taskData.getTask().getTaskName();
        this.createdBy = taskData.getCreatedBy().getName();
        this.type = taskData.getTask().getName();
        this.targets = String.valueOf(taskData.getTargetGraph().totalSize());
        this.leaves = String.valueOf(taskData.getTargetGraph().getTypesStatistics().get(Type.leaf));
        this.middles = String.valueOf(taskData.getTargetGraph().getTypesStatistics().get(Type.middle));
        this.independents = String.valueOf(taskData.getTargetGraph().getTypesStatistics().get(Type.independent));
        this.roots = String.valueOf(taskData.getTargetGraph().getTypesStatistics().get(Type.root));
        this.creditPerTarget = String.valueOf(taskData.getTargetGraph().getPrices().get(taskData.getTask().getClass()));
        this.status = taskData.getStatus().toString();
        this.workers = String.valueOf(taskData.getWorkerListMap().keySet().size());
        this.registered = "No";
    }
}
