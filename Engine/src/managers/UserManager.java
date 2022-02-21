package managers;

import types.Admin;
import types.IUser;
import types.Worker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private final Map<String, IUser> userNameToUser;

    public UserManager() {
        userNameToUser = new HashMap<>();
    }

    public synchronized void addAdmin(String username) {
        userNameToUser.put(username, new Admin(username));
    }

    public synchronized void addWorker(String username,int threads) {
        userNameToUser.put(username, new Worker(username,threads));
    }

//    public synchronized void removeUser(String username) {
//        usersSet.remove(username);
//    }

    public synchronized Map<String, IUser> getUsers() {
        return Collections.unmodifiableMap(userNameToUser);
    }

    public synchronized Worker getWorker(String userName) {
        if (userNameToUser.getOrDefault(userName, null) instanceof Worker) {
            return (Worker) userNameToUser.get(userName);
        } else {
            return null;
        }
    }

    public synchronized Admin getAdmin(String userName) {
        if (userNameToUser.getOrDefault(userName, null) instanceof Admin) {
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
