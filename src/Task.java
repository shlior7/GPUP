
enum Result {
    Failure,
    Warning,
    Success
}

public interface Task {
    public String getName();

    public Result run(Target target) throws InterruptedException;
}
