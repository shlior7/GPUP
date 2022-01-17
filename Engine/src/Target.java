import java.time.Duration;
import java.time.Instant;

public class Target {
    public String name;
    private Result result;
    private Status status;
    private String userData;
    private Duration processTime;

    public Target(String name) {
        this.name = name;
        this.setProcessTime(Duration.ZERO);
    }

    public String getUserData() {

        return userData;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public void setResultFromStr(String result) {
        if (result == null) {
            this.result = null;
            return;
        }
        try {
            this.result = Result.valueOf(result);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public String geStringInfo() {
        return
                "\nName= '" + name + '\'' +
                        "\nUser Data= '" + userData + '\'';
    }

    @Override
    public String toString() {
        return name;
    }
//
//    public void run(Task task) throws InterruptedException {
//        Instant before = Instant.now();
//        result = task.run(this);
//        Instant after = Instant.now();
//        setProcessTime(Duration.between(before, after));
//    }

    public Duration getProcessTime() {
        return processTime;
    }

    public void setProcessTime(Duration processTime) {
        this.processTime = processTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
