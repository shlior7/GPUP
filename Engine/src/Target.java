import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
public class Target {
    public String name;
    private String targetInfo;
    private Result result;
    private Status status;
    private String userData;
    private Duration processTime;

    public Target(String name) {
        this.name = name;
        this.processTime = Duration.ZERO;
        this.result = Result.NULL;
    }


    public void setResultFromStr(String result) {
        if (result == null) {
            this.result = Result.NULL;
            return;
        }
        try {
            this.result = Result.valueOf(result);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public String geStringInfo() {
        return
                "Name: " + name +
                        "\nUser Data: " + userData +
                        "\nStatus: " + status +
                        "\nResult: " + result;

    }

    public void init(String info) {
        this.result = Result.NULL;
        this.status = Status.FROZEN;
        this.targetInfo = info;
    }

    @Override
    public String toString() {
        return name;
    }
}
