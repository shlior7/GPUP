package types;

import TargetGraph.Result;
import TargetGraph.Status;
import TargetGraph.Target;
import lombok.ToString;

@ToString
public class TargetInfo {
    private final String taskName;
    private final String targetName;
    private final String taskType;
    private final String price;
    private String targetStatus;
    private String log;


    public TargetInfo(Task task) {
        Target target = task.getTarget();
        this.taskName = task.getName();
        this.targetName = target.toString();
        this.price = String.valueOf(task.getPricePerTarget());
        this.taskType = task.getType();
        if (target.getStatus() == Status.FINISHED) {
            this.targetStatus = target.getResult().toString();
            if(target.getResult() == Result.Success||target.getResult() == Result.Warning) {
                this.log = targetName + ".log";
            }
            else{
                this.log = "";
            }
        }
        else {
            this.targetStatus = target.getStatus().toString();
            this.log = "";
        }
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTaskType() {
        return taskType;
    }

    public String getPrice() {
        return price;
    }

    public String getTargetStatus() {
        return targetStatus;
    }

    public String getLog() {return log;}

    public synchronized void setLog(String log) {
        this.log = log;
    }

    public synchronized void setTargetStatus(String targetStatus) {
        this.targetStatus = targetStatus;
    }

}