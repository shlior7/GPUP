import java.util.ArrayList;

public class Target {
    public String name;
    public ArrayList<Target> DependsOn;

    public Target(String name) {
        this.name = name;
    }

    public void AddDependsOn(Target t) {
        this.DependsOn.add(t);
    }

    public String myStatus() {

    }

}
