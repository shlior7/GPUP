enum Status {
    WAITING("blue"),
    FROZEN("blue"),
    SKIPPED("orange"),
    IN_PROCESS("yellow"),
    FINISHED("darkgray");

    private String color;

    private Status(final String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
