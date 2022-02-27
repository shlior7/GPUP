package engine;

import TargetGraph.Status;
import TargetGraph.Target;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.scene.control.TableView;
import types.TargetInfo;
import types.Task;
import types.TaskInfo;
import types.TaskStatus;
import utils.Constants;
import utils.ObservableAtomicInteger;
import utils.http.HttpClientUtil;
import utils.http.SimpleCallBack;
import utils.Utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static utils.Constants.GSON_INSTANCE;
import static utils.Utils.setAddRemoveFromTable;
import static utils.Utils.setAndAddToTable;

public class TaskProcessor {
    private final ConcurrentMap<String, List<Target>> signedTasksTargets;
    private final ConcurrentMap<Target, String> targetsToTaskName;
    private final ConcurrentMap<String, Task> runningTasks;
    private final Map<String, ObservableAtomicInteger> creditsPerTask;
    private final ConcurrentMap<String, List<Target>> targetsHistory;
    private final ConcurrentMap<String, Task> tasksHistory;

    private final ObservableAtomicInteger availableThreads;
    private final ObservableAtomicInteger credits;
    private final Queue<Target> queue;
    public final AtomicBoolean pause;
    private final int numThread;
    private boolean running;
    private final TableView<TargetInfo> targetsTable;
    private final TableView<TaskInfo> myTasksTable;
    private Timer updateTimer;
    int prev;

    private ExecutorService threadExecutor;

    public TaskProcessor(int numThread, TableView<TargetInfo> targetTable, TableView<TaskInfo> myTasksTable) {
        this.availableThreads = new ObservableAtomicInteger(numThread);
        this.myTasksTable = myTasksTable;
        this.credits = new ObservableAtomicInteger(0);
        this.pause = new AtomicBoolean(false);
        this.targetsTable = targetTable;
        this.targetsToTaskName = new ConcurrentHashMap<>();
        this.creditsPerTask = new HashMap<>();
        this.queue = new LinkedList<>();
        this.runningTasks = new ConcurrentHashMap<>();

        this.targetsHistory = new ConcurrentHashMap<>();
        this.tasksHistory = new ConcurrentHashMap<>();
        this.signedTasksTargets = new ConcurrentHashMap<>();
        this.numThread = numThread;
        this.running = false;
        this.updateTimer = new Timer();
        initTimers();
    }

    public void initTimers() {
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateProgress();
            }
        }, 0, 5 * 1000);


    }

    public synchronized ObservableAtomicInteger availableThreads() {
        return availableThreads;
    }

    public void pushTask(Task task) {
        runningTasks.put(task.getTaskName(), task);
        creditsPerTask.putIfAbsent(task.getTaskName(), new ObservableAtomicInteger(0));
        targetsHistory.putIfAbsent(task.getTaskName(), new ArrayList<>());
        tasksHistory.put(task.getTaskName(), task);
    }

    public void removeTask(String taskName) {
        try {
            signedTasksTargets.remove(taskName);
            runningTasks.remove(taskName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void pushTargets(Map<String, Target[]> targetsMap) throws InterruptedException {
        if (targetsMap == null)
            return;
        targetsMap.forEach((taskName, targets) -> {
            for (Target target : targets) {
                pushTarget(taskName, target);
            }
        });
        setTargetsTable();
        start();
    }

    public synchronized void pushTarget(String taskName, Target target) {
        System.out.println("pushing to task " + taskName + " the target " + target);
        targetsToTaskName.put(target, taskName);

        signedTasksTargets.putIfAbsent(taskName, new ArrayList<>());
        signedTasksTargets.get(taskName).add(target);

        queue.add(target);
        availableThreads.decrement();
    }

    public void start() throws InterruptedException {
        if (running)
            return;

        System.out.println("STARTED!!!!");
        System.out.println(availableThreads.get());
        running = true;
        this.threadExecutor = Executors.newFixedThreadPool(numThread);
        Thread runningThread = new Thread(() -> {
            while (signedTasksTargets.size() > 0 || !queue.isEmpty()) {
                if (!queue.isEmpty()) {
                    Target target = queue.poll();
                    runTaskOnTarget(target);
                }
            }
            threadExecutor.shutdown();
            while (!threadExecutor.isTerminated()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("FINISHED!!!!");

            running = false;
            pause.set(false);
        }, "TargetProcessor");
        runningThread.start();
    }


    public synchronized void runTaskOnTarget(Target target) {
//        synchronized (this) {
//            setTaskOutput("running task on target: " + target.name);
//        }
        System.out.println("running task on target: " + target.name);
        threadExecutor.execute(initTask(target));
    }

    public void resume(String taskName) {
        runningTasks.put(taskName, tasksHistory.get(taskName));
    }

    public void pause(String taskName) {
        runningTasks.remove(taskName);
    }

    public boolean taskIsRunning(String taskName) {
        return runningTasks.containsKey(taskName);
    }

    public boolean togglePause(String taskName) {
        if (runningTasks.containsKey(taskName)) {
            pause(taskName);
            return false;
        }
        resume(taskName);
        return true;
    }

    public Task initTask(Target target) {
        Task newTask = tasksHistory.get(targetsToTaskName.get(target)).copy();
        newTask.setTarget(target);
        newTask.setFuncOnFinished(this::OnFinish);
        newTask.setOutputText(this::setTaskOutput);
        return newTask;
    }

    public synchronized void setTaskOutput(String text) {
//        taskOutput.set(text);
    }

    public int getTasksReceivedCredits(String taskName) {
        if (creditsPerTask.containsKey(taskName)) {
            return creditsPerTask.get(taskName).get();
        }
        return 0;
    }

    public int getTasksTargetsDone(String taskName) {
        if (creditsPerTask.containsKey(taskName)) {
            return creditsPerTask.get(taskName).get() / tasksHistory.get(taskName).getCreditPerTarget();
        }
        return 0;
    }

    public void OnFinish(Target target) {
        System.out.println("finished " + target.getName());
        availableThreads.increment();
        System.out.println(availableThreads.get() + "\n");

        target.setStatus(Status.FINISHED);
        Task finishedTask = tasksHistory.get(targetsToTaskName.get(target));
        creditsPerTask.get(finishedTask.getTaskName()).addAndGet(finishedTask.getCreditPerTarget());
        credits.addAndGet(finishedTask.getCreditPerTarget());

//        setTaskOutput("finished task " + name + " with the result " + target.getResult() + " time it took to process " + target.getProcessTime().toMillis());

        try {
            String url = HttpClientUtil.createUrl(
                    Constants.TARGET_DONE_URL,
                    Utils.tuple(Constants.TARGETNAME, target.getName()),
                    Utils.tuple(Constants.TASKNAME, targetsToTaskName.get(target)),
                    Utils.tuple(Constants.RESULT, target.getResult().toString()));
            System.out.println(url);
            Platform.runLater(() -> {
                HttpClientUtil.runAsync(url, new SimpleCallBack());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateProgress();
        getMoreTargets();
        setTargetsTable();

        String taskName = targetsToTaskName.get(target);
        if (signedTasksTargets.containsKey(taskName)) {
            signedTasksTargets.get(taskName).remove(target);
            if (!runningTasks.containsKey(taskName) && signedTasksTargets.get(taskName).isEmpty() && myTasksTable.getItems().stream().noneMatch(t -> t.getTaskName().equals(taskName)))
                signedTasksTargets.remove(taskName);
        }
        targetsHistory.get(taskName).add(target);
        targetsToTaskName.remove(target);

        setMyTasksTable();
    }

    AtomicBoolean waitingForResponse = new AtomicBoolean(false);

    public synchronized void getMoreTargets() {
        if (prev != availableThreads.get()) {
            System.out.println("available " + availableThreads.get());
            prev = availableThreads.get();
        }

        if (availableThreads.get() <= 0 || runningTasks.size() == 0 || waitingForResponse.get())
            return;

        waitingForResponse.set(true);
        String url = HttpClientUtil.createUrl(Constants.GET_TARGETS, Utils.tuple(Constants.TARGETS, String.valueOf(availableThreads.get())));

        List<String> tasks = runningTasks.values().stream().map(Task::getTaskName).collect(Collectors.toList());

        System.out.println(" when asking " + availableThreads.get() + " ," + runningTasks.keySet());
        HttpClientUtil.runAsyncBody(url, GSON_INSTANCE.toJson(tasks), new SimpleCallBack((targetsMapJson) -> {
            try {
                Map<String, Target[]> targets = GSON_INSTANCE.fromJson(targetsMapJson, new TypeToken<Map<String, Target[]>>() {
                }.getType());
                pushTargets(targets);
                waitingForResponse.set(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    public synchronized void updateProgress() {
        if (signedTasksTargets.values().stream().allMatch(List::isEmpty))
            return;

        setTargetsTable();
        String url = HttpClientUtil.createUrl(
                Constants.UPDATE_PROGRESS_POST_URL);
        System.out.println(url);

        HttpClientUtil.runAsyncBody(url, GSON_INSTANCE.toJson(signedTasksTargets), new SimpleCallBack());
    }


    public synchronized void setTargetsTable() {
        if (targetsToTaskName.isEmpty())
            return;

        System.out.println("set target table\n targetsToTaskName " + targetsToTaskName);
        List<TargetInfo> targets = targetsToTaskName.keySet().stream().map(t -> new TargetInfo(tasksHistory.get(targetsToTaskName.get(t)), t)).collect(Collectors.toList());
        System.out.println("targets " + targets);

        setAndAddToTable(targets,
                targetsTable,
                (t1, t2) -> {
                    t1.setTargetStatus(t2.getTargetStatus());
                    t1.setCredits(t2.getCredits());
                });
    }

    public void setMyTasksTable(List<TaskInfo> myTasks) {
        setAddRemoveFromTable(myTasks, myTasksTable
                , (t1, t2) -> t1.setProgress(t2.getProgress())
                , (t1, t2) -> t1.setWorkers(t2.getWorkers()));
    }

    public void setMyTasksTable() {
        setAndAddToTable(myTasksTable.getItems(), myTasksTable,
                (t1, t2) -> t1.setCreditsFromTask(String.valueOf(creditsPerTask.get(t1.getTaskName()).get())),
                (t1, t2) -> t1.setTargetsProcessed(String.valueOf(targetsHistory.get(t1.getTaskName()).size())));
        myTasksTable.refresh();
    }

    public StringBinding getCreditsBinding() {
        return credits.asString();
    }


    public synchronized void signToTasks(String taskName, boolean signTo) {
        String finalUrl = HttpClientUtil.createUrl(Constants.TASK_SIGN
                , Utils.tuple(Constants.SIGNTO, String.valueOf(signTo))
                , Utils.tuple(Constants.TASKNAME, taskName));

        System.out.println("finalUrl " + finalUrl);

        HttpClientUtil.runAsync(finalUrl, new SimpleCallBack((taskJson) -> {
            System.out.println(taskJson);
            if (signTo) {
                JsonObject json = GSON_INSTANCE.fromJson(taskJson, JsonObject.class);
                System.out.println("taskJson = " + taskJson);
                Task task = Utils.getTaskFromJson(json);
                task.setCreditPerTarget(json.get("creditPerTarget").getAsInt());
                pushTask(task);
            } else {
                removeTask(taskName);
            }
        }));
    }

}
