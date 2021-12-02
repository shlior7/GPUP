public interface Task {
    String getName();
    Result run(Target target) throws InterruptedException;
}
