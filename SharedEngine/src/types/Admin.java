package types;

public class Admin implements IUser {
    private final String name;

    public Admin(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getRole() {
        return "Admin";
    }
}
