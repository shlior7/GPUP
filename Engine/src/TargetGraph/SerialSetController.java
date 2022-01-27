package TargetGraph;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SerialSetController {
    private final List<SerialSet> serialSets;

    public SerialSetController(List<SerialSet> serialSets) {
        this.serialSets = serialSets;
    }

    public List<SerialSet> getSerialSetsOf(String targetName) {
        return getSetsContainingStream(targetName).collect(Collectors.toList());
    }

    public synchronized boolean isBusy(String targetName) {
        return getSetsContainingStream(targetName).anyMatch(SerialSet::isBusy);
    }

    public synchronized void setBusy(String targetName, boolean busy) {
        getSetsContainingStream(targetName).forEach(serialSet -> serialSet.setBusy(busy));
    }

    public synchronized Stream<SerialSet> getSetsContainingStream(String targetName) {
        return serialSets.stream().filter(serialSet -> serialSet.getTargets().contains(targetName));
    }

    public List<String> getSerialSets() {
        return serialSets.stream().map(SerialSet::toString).collect(Collectors.toList());
    }

}
