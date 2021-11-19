import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

enum Type {
    leaf,
    root,
    middle,
    independent
}

enum Status {
    WAITING,
    FROZEN,
    SKIPPED,
    IN_PROCESS,
    FINISHED(null) {
        @Override
        public boolean isFinished() {
            return true;
        }
    };
    private Result result;

    Status() {

    }

    Status(Result result) {
        this.result = result;
    }


    public boolean isFinished() {
        return false;
    }

    public boolean DidFailed() {
        return result == Result.Failure;
    }

    public Result getFinishedResult() {
        return result;
    }

    public void setFinishedResult(Result result) {
        this.result = result;
    }

}

public class Target {
    public String name;
    public int id;
    public Status status;
    private String userData;

    public Target(String name, int id) {
        this.name = name;
        this.id = id;
        this.status = Status.WAITING;

    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    @Override
    public String toString() {
        return
                "\nname= '" + name + '\'' +
                        "\nid= " + id +
                        "\nstatus= " + status +
                        "\nuserData= '" + userData + '\'';
    }

    public String run(Task task) throws InterruptedException {
        status = Status.IN_PROCESS;
        Result result = task.run(this);
        String executionTime = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now());
        UI.print(executionTime);
        status = Status.FINISHED;
        status.setFinishedResult(result);
        return "done";
    }
}
