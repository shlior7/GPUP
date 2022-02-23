package types;

import TargetGraph.Result;
import TargetGraph.Status;
import TargetGraph.Target;
import TargetGraph.TargetGraph;

public class GraphInfoWorker {
    private final String taskName;
    private String workersOnTask;
    private String progress;
    private String targetFinished;
    private String credits;


    public GraphInfoWorker(Task task, TargetGraph target) {
        this.taskName = task.getName();
        this.workersOnTask = task.toString();

    }

//    public String getTaskName() {
//        return taskName;
//    }
//
//    public String getTargetName() {
//        return targetName;
//    }
//
//    public String getTaskType() {
//        return taskType;
//    }
//
//    public String getPrice() {
//        return price;
//    }
//
//    public String getTargetStatus() {
//        return targetStatus;
//    }
//
//    public String getLog() {return log;}
//
//    public synchronized void setLog(String log) {
//        this.log = log;
//    }
//
//    public synchronized void setTargetStatus(String targetStatus) {
//        this.targetStatus = targetStatus;
//    }
}
