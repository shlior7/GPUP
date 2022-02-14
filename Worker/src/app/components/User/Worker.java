package app.components.User;

public class Worker extends User {
    int threads;

    public Worker(String userName, int threads) {
        super(userName);
        this.position = "Worker";
        this.threads = threads;
    }
}
