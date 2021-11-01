import java.util.HashSet;
import java.util.Set;


public class Target {
    public String name;
    public Set<Target> RequiredForSet;
    public Set<Target> DependsOnSet;
    public Status status;

    enum Type {
        leaf,
        root,
        middle,
        independent
    }

    enum Result {
        Failure,
        Warning,
        Success
    }

    enum Status {
        WAITING,
        FROZEN,
        SKIPPED,
        IN_PROCESS,
        FINISHED(null) {
            @Override
            public boolean isFinished() {
                return true;
            }
        };
        private Result result;

        public boolean isFinished() {
            return false;
        }

        public Result getFinishedResult() {
            return result;
        }

        Status() {
            this.result = null;
        }

        Status(Result result) {
            this.result = result;
        }
    }


    public Target(String name) {
        this.name = name;
        this.status = Status.WAITING;
        this.DependsOnSet = new HashSet<>();
        this.RequiredForSet = new HashSet<>();

    }

    public Target DependsOn(Target t) {
        this.DependsOnSet.add(t);
        t.RequiredFor(this);
        return t;
    }

    private void RequiredFor(Target t) {
        this.RequiredForSet.add(t);
    }

    public boolean DoDependsOn(Target t) {
        if (!hasDependencies()) {
            return false;
        }
        return DependsOnSet.contains(t) | DependsOnSet.stream().map(this::DoDependsOn).reduce(false, (a, b) -> a | b);
    }

    public boolean hasDependencies() {
        return DependsOnSet.size() > 0;
    }

    public boolean hasRequiredOn() {
        return RequiredForSet.size() > 0;
    }

    public Type MyType() {
        boolean depends = hasDependencies();
        boolean required = hasRequiredOn();
        if (depends && required) {
            return Type.middle;
        }
        if (depends) {
            return Type.root;
        }
        if (required) {
            return Type.leaf;
        }
        return Type.independent;
    }


}
