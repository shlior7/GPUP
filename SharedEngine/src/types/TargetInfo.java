package types;

import TargetGraph.Result;
import TargetGraph.Status;
import TargetGraph.Target;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class TargetInfo extends TableItem {
    private final String targetName;
    private final String taskName;
    private final String taskType;
    private final String price;
    private StringProperty credits;
    private StringProperty targetStatus;
    private String log;

    public TargetInfo(Task task, Target target) {
        super(task.getTaskName() + "" + target.getName());
        this.taskName = task.getTaskName();
        this.targetName = target.getName();
        this.price = String.valueOf(task.getCreditPerTarget());
        this.taskType = task.getClassName();
        setCredits(target.getStatus() == Status.FINISHED ? String.valueOf(task.getCreditPerTarget()) : "0");
        if (target.getStatus() == Status.FINISHED) {
            setTargetStatus(target.getResult().toString());
            if (target.getResult() == Result.Success || target.getResult() == Result.Warning) {
                this.log = targetName + ".log";
            } else {
                this.log = "";
            }
        } else {
            setTargetStatus(target.getStatus().toString());
            this.log = "";
        }
    }

    public String getCredits() {
        return creditsProperty().get();
    }

    public synchronized void setCredits(String credit) {
        creditsProperty().set(credit);
    }

    public StringProperty creditsProperty() {
        if (credits == null) credits = new SimpleStringProperty(this, "credits");
        return credits;
    }

    public String getTargetStatus() {
        return targetStatusProperty().get();
    }

    public synchronized void setTargetStatus(String targetStatus) {
        targetStatusProperty().set(targetStatus);
    }

    public StringProperty targetStatusProperty() {
        if (targetStatus == null) targetStatus = new SimpleStringProperty(this, "targetStatus");
        return targetStatus;
    }
}