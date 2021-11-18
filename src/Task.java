
enum Result {
    Failure,
    Warning,
    Success
}

public interface Task {
    public void run() throws InterruptedException;
}
