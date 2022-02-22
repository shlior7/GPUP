package types;


import TargetGraph.TargetGraph;
import TargetGraph.Target;
import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private TaskStatus status = TaskStatus.UNSTARTED;
    private Map<Worker, List<Target>> workerListMap = new HashMap<>();

    public void setWorkersTargets(Worker worker, List<Target> targets) {
        workerListMap.put(worker, targets);
    }

    public void removeWorker(Worker worker){
        try {
            workerListMap.remove(worker);
        }
        catch (Exception ignored){}
    }
}
