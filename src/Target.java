import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
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
    FINISHED
}

public class Target {
    public String name;
    public int id;
    public Status status;
    public Result result;
    private String userData;
    private Duration processTime;

    public Target(String name, int id) {
        this.name = name;
        this.id = id;
        this.status = Status.FROZEN;
        this.processTime = Duration.ZERO;
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

    public void run(Task task) throws InterruptedException, IOException {
        status = Status.IN_PROCESS;
        Instant before = Instant.now();
        result = task.run(this);
        Instant after = Instant.now();
        processTime = Duration.between(before, after);
        status = Status.FINISHED;
    }

    public Duration getProcessTime() {
        return processTime;
    }
}
