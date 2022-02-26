package types;


import TargetGraph.TargetGraph;
import TargetGraph.Target;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

    public void setWorkersTargets(Worker worker, List<Target> targets) {
        workerListMap.put(worker, targets);
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
}
