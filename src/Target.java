import java.util.HashSet;
import java.util.Set;

enum Type {
    leaf,
    root,
    middle,
    independent
}

enum Status {
    WAITING,
    FROZEN,
    SKIPPED,
    IN_PROCESS,
    FINISHED
}

public class Target {
    public String name;
    public int id;
    //    public Set<Target> RequiredForSet;
//    public Set<Target> DependsOnSet;
    public Status status;
    private String userData;

    public Target(String name, int id) {
        this.name = name;
        this.id = id;
        this.status = Status.WAITING;
//        this.DependsOnSet = new HashSet<>();
//        this.RequiredForSet = new HashSet<>();

    }

//    public Target DependsOn(Target t) {
//        this.DependsOnSet.add(t);
//        t.RequiredFor(this);
//        return t;
//    }
//
//    private void RequiredFor(Target t) {
//        this.RequiredForSet.add(t);
//    }
//
//    public boolean DoDependsOn(Target t) {
//        if (!hasDependencies()) {
//            return false;
//        }
//        return DependsOnSet.contains(t) | DependsOnSet.stream().map(this::DoDependsOn).reduce(false, (a, b) -> a | b);
//    }

//    public boolean hasDependencies() {
//        return DependsOnSet.size() > 0;
//    }
//
//    public boolean hasRequiredOn() {
//        return RequiredForSet.size() > 0;
//    }


    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    @Override
    public String toString() {
        return
                "\nname= '" + name + '\'' +
                        "\nid= " + id +
                        "\nstatus= " + status +
                        "\nuserData= '" + userData + '\'';
    }

    public String run(Task task) throws InterruptedException {
        status = Status.IN_PROCESS;
        task.run();
        status = Status.FINISHED;
        return "done";
    }
}
