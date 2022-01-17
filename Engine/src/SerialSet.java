import java.util.HashSet;

public class SerialSet {
    private final String name;
    private final HashSet<String> targets;
    private boolean busy;

    public SerialSet(String name, HashSet<String> targets) {
        this.name = name;
        this.targets = targets;
    }

    public void push(String item) {
        targets.add(item);
    }

    public HashSet<String> getTargets() {
        return targets;
    }

    public String getName() {
        return name;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }
}
