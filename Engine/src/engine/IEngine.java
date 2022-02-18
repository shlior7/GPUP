package engine;

import TargetGraph.TargetGraph;
import Users.UserManager;
import types.Admin;
import types.IUser;

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

public interface IEngine {
    IUser getUser(String name);

    Set<IUser> getAllUsers();

    Collection<TargetGraph> getAllGraphs();

    UserManager getUserManager();

    void loadXmlFile(InputStream path, Admin createdBy) throws Exception;

}
