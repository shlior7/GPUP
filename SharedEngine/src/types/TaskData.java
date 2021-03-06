package types;


import TargetGraph.TargetGraph;
import TargetGraph.Target;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
public class TaskData {
    @NonNull
    protected Task task;
    @NonNull
    protected TargetGraph targetGraph;
    @NonNull
    protected Admin createdBy;
    @NonNull
    protected AtomicInteger targetsDone;

    private TaskStatus status = TaskStatus.UNSTARTED;
    private Map<Worker, List<Target>> workerListMap = new HashMap<>();
    private Map<String, List<String>> workerTargets = new HashMap<>();

    public void setWorkersTargets(Worker worker, List<Target> targets) {
        workerListMap.put(worker, targets);
        workerTargets.put(worker.getName(), targets.stream().map(Target::getName).collect(Collectors.toList()));
    }

    public void removeWorker(Worker worker) {
        try {
            workerListMap.remove(worker);
        } catch (Exception ignored) {
        }
    }

    public AtomicInteger getTargetsDoneInteger() {
        return targetsDone;
    }

    public int getTargetsDone() {
        return targetsDone.get();
    }

    public void setTargetsDone(int value) {
        targetsDone.set(value);
    }

    public Map<String, List<String>> getWorkersTargets() {
        Map<String, List<String>> workersTargets = new HashMap<>();
        workerListMap.keySet().forEach(worker -> {
            workersTargets.put(worker.getName(), workerListMap.get(worker).stream().map(Target::getName).collect(Collectors.toList()));
        });
        return workersTargets;
    }
}
