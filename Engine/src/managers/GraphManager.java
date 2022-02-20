package managers;

import types.Admin;
import types.IUser;
import types.Worker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GraphManager {
    private final Map<String, IUser> userNameToUser;

    public GraphManager() {
        userNameToUser = new HashMap<>();
    }

    public synchronized void addUser(String username, boolean isAdmin) {
        IUser newUser;
        if (isAdmin) {
            newUser = new Admin(username);
        } else {
            newUser = new Worker(username);
        }
        userNameToUser.put(username, newUser);
    }

//    public synchronized void removeUser(String username) {
//        usersSet.remove(username);
//    }

    public synchronized Map<String, IUser> getUsers() {
        return Collections.unmodifiableMap(userNameToUser);
    }

    public synchronized Worker getWorker(String userName) {
        if (userNameToUser.get(userName) instanceof Worker) {
            return (Worker) userNameToUser.get(userName);
        } else {
            return null;
        }
    }

    public synchronized Admin getAdmin(String userName) {
        if (userNameToUser.get(userName) instanceof Admin) {
            return (Admin) userNameToUser.get(userName);
        } else {
            return null;
        }
    }


    public boolean isUserAdmin(String userName) {
        return userNameToUser.get(userName) instanceof Admin;
    }

    public boolean isUserExists(String username) {
        return userNameToUser.containsKey(username);
    }
}
