package TargetGraph;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;

@Getter
@Setter
public class Target {
    public String name;
    private String targetInfo;
    private String userData;
    private Result result;
    private Status status;
    private Duration processTime;
    private Instant startedTime;
    private Instant waitingTime;

    public Target(String name) {
        this.name = name;
        this.processTime = Duration.ZERO;
        this.result = Result.NULL;
    }

    public void updateData(Target target) {
        this.status = target.status;
        this.result = target.result;
        this.processTime = target.processTime;
        this.startedTime = target.startedTime;
        this.waitingTime = target.waitingTime;
        this.targetInfo = target.targetInfo;
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
        if (!info.isEmpty())
            this.targetInfo = info;
    }

    @Override
    public String toString() {
        return name;
    }
}
