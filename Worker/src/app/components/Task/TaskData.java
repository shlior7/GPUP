package app.components.Task;

import app.components.User.User;

import java.util.Map;

public class TaskData {
    String name;
    User createdBy;
    String type;
    int targets;
    Map<String, String[]> targetsPerType;
    int creditPerTarget;
    TaskStatus status;
    int workers;
    boolean registered;

    public TaskData(String name, User createdBy, String type, int targets, Map<String, String[]> targetsPerType, int creditPerTarget, TaskStatus status, int workers, boolean registered) {
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
