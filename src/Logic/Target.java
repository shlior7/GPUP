package Logic;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;




public class Target {
    public String name;
    private Result result;
    private String userData;
    private Duration processTime;

    public Target(String name) {
        this.name = name;
        this.processTime = Duration.ZERO;
    }

    public String getUserData() {

        return userData;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(String result) {
        if(result == null) {
            this.result = null;
            return;
        }
        try {
            this.result = Result.valueOf(result);
        }
        catch (IllegalArgumentException ignored){
        }
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
        Instant before = Instant.now();
        result = task.run(this);
        Instant after = Instant.now();
        processTime = Duration.between(before, after);
    }

    public Duration getProcessTime() {
        return processTime;
    }

}
