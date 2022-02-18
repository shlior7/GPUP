package types;

public class Worker implements IUser {
    private final String name;

    public Worker(String name) {
        this.name = name;
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
