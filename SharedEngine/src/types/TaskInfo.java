package types;


import java.util.Map;

public class TaskInfo {
    public String name;
    Admin createdBy;
    String type;
    int targets;
    Map<String, String[]> targetsPerType;
    int creditPerTarget;
    TaskStatus status;
    int workers;
    boolean registered;

    public TaskInfo(String name, Admin createdBy, String type, int targets, Map<String, String[]> targetsPerType, int creditPerTarget, TaskStatus status, int workers, boolean registered) {
        this.name = name;
        this.createdBy = createdBy;
        this.type = type;
        this.targets = targets;
        this.targetsPerType = targetsPerType;
        this.creditPerTarget = creditPerTarget;
        this.status = status;
        this.workers = workers;
        this.registered = registered;
    }
}
