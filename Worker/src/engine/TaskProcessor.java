package engine;

import TargetGraph.Target;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import okhttp3.HttpUrl;
import types.Task;
import utils.Constants;
import utils.ObservableAtomicInteger;
import utils.http.HttpClientUtil;
import utils.http.SimpleCallBack;
import utils.Utils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static utils.Constants.GSON_INSTANCE;

public class TaskProcessor {
    private final Map<String, List<Target>> tasksTargets;
    private final Map<Target, String> targetsToTaskName;
    private final ObservableAtomicInteger targetsDone;
    public final Object pauseLock = new Object();  /* delete? */
    private final Map<String, Task> tasks;
    private final Queue<Target> queue;
    public final AtomicBoolean pause;
    private final int numThread;
    private boolean running;

    private ExecutorService threadExecutor;

    public TaskProcessor(int numThread) {
        this.targetsDone = new ObservableAtomicInteger(0);
        this.pause = new AtomicBoolean(false);
        this.targetsToTaskName = new HashMap<>();
        this.queue = new LinkedList<>();
        this.tasks = new HashMap<>();
        this.tasksTargets = new HashMap<>();
        this.numThread = numThread;
        this.running = false;
    }

    public synchronized ObservableAtomicInteger availableThreads() {
        return targetsDone;
    }

    public void pushTask(Task task) {
        tasks.put(task.getTaskName(), task);
    }

    public void removeTask(String taskName) {
        try {
            tasks.remove(taskName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pushTargets(Map<String, List<Target>> targetsMap) throws InterruptedException {
        targetsMap.forEach((taskName, targets) -> {
            targets.forEach((target) -> {
                pushTarget(taskName, target);
            });
        });
        start();
    }

    public void pushTarget(String taskName, Target target) {
        targetsToTaskName.put(target, taskName);

        tasksTargets.putIfAbsent(taskName, new ArrayList<>());
        tasksTargets.get(taskName).add(target);

        queue.add(target);
    }

    public void start() throws InterruptedException {
        if (running)
            return;
        System.out.println("STARTED!!!!");
        running = true;
        this.threadExecutor = Executors.newFixedThreadPool(numThread);
        while (tasks.size() > 0 || !queue.isEmpty()) {
            if (pause.get()) {
                try {
                    synchronized (this) {
//                        setTaskOutput("Paused!");
                        pauseLock.wait();
//                        setTaskOutput("Resumed!");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (!queue.isEmpty()) {
                Target target = queue.poll();
                runTaskOnTarget(target);
            }
        }
        threadExecutor.shutdown();
        while (!threadExecutor.isTerminated()) {
            Thread.sleep(1000);
        }
        System.out.println("FINISHED!!!!");

        running = false;
        pause.set(false);
    }

    public synchronized void runTaskOnTarget(Target target) {
//        synchronized (this) {
//            setTaskOutput("running task on target: " + target.name);
//        }
        threadExecutor.execute(initTask(target));
        targetsDone.incrementAndGet();
    }

    public void resume() {
        synchronized (this) {
            pauseLock.notifyAll();
        }
    }

    public boolean togglePause() {
        pause.set(!pause.get());
        if (!pause.get()) {
            resume();
        }
        return pause.get();
    }

    public Task initTask(Target target) {
        Task newTask = tasks.get(targetsToTaskName.get(target)).copy();
        newTask.setTarget(target);
        newTask.setFuncOnFinished(this::OnFinish);
        newTask.setOutputText(this::setTaskOutput);
        return newTask;
    }

    public synchronized void setTaskOutput(String text) {
//        taskOutput.set(text);
    }

    public void OnFinish(Target target) {
        System.out.println("finished " + target.geStringInfo());

        targetsDone.incrementAndGet();
        if (tasksTargets.get(targetsToTaskName.get(target)).size() == 0)
            tasksTargets.remove(targetsToTaskName.get(target));
        targetsToTaskName.remove(target);
        updateProgress();
//        setTaskOutput("finished task " + name + " with the result " + target.getResult() + " time it took to process " + target.getProcessTime().toMillis());

        try {
            String url = HttpClientUtil.createUrl(
                    Constants.TARGET_DONE_URL,
                    Utils.tuple(Constants.TARGETNAME, target.getName()),
                    Utils.tuple(Constants.TASKNAME, targetsToTaskName.get(target)));

            Platform.runLater(() -> {
                HttpClientUtil.runAsync(url, new SimpleCallBack());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        getMoreTargets();
    }

    public synchronized void getMoreTargets() {
        if (targetsDone.get() <= 0)
            return;

        String finalUrl = HttpUrl
                .parse(Constants.GET_TARGETS)
                .newBuilder()
                .addQueryParameter(Constants.TARGETS, String.valueOf(targetsDone.getAndSet(0)))
                .build()
                .toString();
        System.out.println("finalUrl " + finalUrl);

        Platform.runLater(() -> {
            HttpClientUtil.runAsync(finalUrl, new SimpleCallBack((targetsMapJson) -> {
                try {
                    Map<String, List<Target>> targets = GSON_INSTANCE.fromJson(targetsMapJson, new TypeToken<Map<String, List<Target>>>() {
                    }.getType());
                    pushTargets(targets);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        });
    }

    public synchronized void updateProgress() {
        try {
            String url = HttpClientUtil.createUrl(
                    Constants.UPDATE_PROGRESS_POST_URL);

            Platform.runLater(() -> {
                HttpClientUtil.runAsyncBody(url, GSON_INSTANCE.toJson(targetsToTaskName), new SimpleCallBack());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
