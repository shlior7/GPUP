import javax.xml.bind.annotation.XmlRootElement;
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
    //    private Status status;
    private Result result;
    private String userData;
    private Duration processTime;

    public Target(String name) {
        this.name = name;
//        this.status = Status.FROZEN;
        this.processTime = Duration.ZERO;
    }

    public String getUserData() {

        return userData;
    }

//    public Status getStatus() {
//        return status;
//    }

    public Result getResult() {
        return result;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    @Override
    public String toString() {
        return
                "\nName= '" + name + '\'' +
                        "\nUser Data= '" + userData + '\'';
    }

    public void run(Task task) throws InterruptedException, IOException {
//        status = Status.IN_PROCESS;
        Instant before = Instant.now();
        result = task.run(this);
        Instant after = Instant.now();
        processTime = Duration.between(before, after);
//        status = Status.FINISHED;
    }

    public Duration getProcessTime() {
        return processTime;
    }

}
