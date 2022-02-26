package types;

public class Worker implements IUser {

    private final String name;
    private int threads;

    public Worker(String name, int threads) {
        this.name = name;
        this.threads = threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public int getThreads() {
        return this.threads;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getRole() {
        return "Worker";
    }
}
