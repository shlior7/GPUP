import java.awt.*;

public enum Result {
    NULL("gray"),
    Failure("red"),
    Warning("orange"),
    Success("green");

    private final String color;

    private Result(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

}

