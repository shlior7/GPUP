package types;

public class LogLine {
    String line;

    public LogLine(String line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return line;
    }
}
