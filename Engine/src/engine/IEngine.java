package engine;

import TargetGraph.TargetGraph;
import TargetGraph.Target;
import managers.UserManager;
import task.TaskManager;
import types.Admin;
import types.IUser;
import types.Task;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IEngine {
    IUser getUser(String name);

    Set<IUser> getAllUsers();

    Collection<TargetGraph> getAllGraphs();

    Map<String, TargetGraph> getGraphManager();

    UserManager getUserManager();

    void loadXmlFile(InputStream path, Admin createdBy) throws Exception;

    void addTask(Task task, String graphName, Admin createdBy, Set<Target> targets, boolean fromScratch) throws Exception;

    TaskManager getTaskManager();

    Map<String, List<Target>> getTargetsForWorker(String userName, String[] taskNames, int threadsAmount) throws Exception;

    void postLogs(Map<String, Map<String, String[]>> logsToPost);

    List<String> getLogs(String taskName) throws IOException;

}
