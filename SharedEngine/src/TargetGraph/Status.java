package TargetGraph;

public enum Status {
    WAITING("blue"),
    FROZEN("blue"),
    SKIPPED("yellow"),
    IN_PROCESS("yellow"),
    FINISHED("darkgray");

    private String color;

    private Status(final String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

}
