package engine;

import TargetGraph.Result;
import TargetGraph.Status;
import TargetGraph.Target;
import javafx.application.Platform;
import okhttp3.HttpUrl;
import types.Task;
import types.TaskInfo;
import types.TaskStatus;
import utils.Constants;
import utils.http.HttpClientUtil;
import utils.http.SimpleCallBack;

import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static utils.Constants.GSON_INSTANCE;

public class TaskProcessor {
    private int numThread;
    private ExecutorService threadExecutor;
    private final AtomicInteger targetsDone;
    private Map<String,Task> tasks;
    private Map<Target,String> targetsToTaskName;
    private Queue<Target> queue;
    public final AtomicBoolean pause = new AtomicBoolean(false);
    public Object pauseLock = new Object();
    private boolean running = false;


    public TaskProcessor(int numThread){
        this.numThread = numThread;
        this.threadExecutor = Executors.newFixedThreadPool(numThread);
        this.targetsDone = new AtomicInteger(0);
    }

    public void pushTask(Task task){
        tasks.put(task.getTaskName(),task);
    }
    public void removeTask(String taskName){
        tasks.put(taskName, null);
    }

    public void pushTarget(Target target,String taskName){
        targetsToTaskName.put(target,taskName);
        queue.add(target);
    }

    public void start() throws InterruptedException {
        while (tasks.size()>0 || !queue.isEmpty()) {
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

        setTaskOutput("Done!");
//        setTaskOutput(targetGraph.getStatsInfoString(targetGraph.getResultStatistics()));
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
        targetsDone.incrementAndGet();
        target.setStatus(Status.FINISHED);
        String name = target.name;
        updateProgress();
        setTaskOutput("finished task " + name + " with the result " + target.getResult() + " time it took to process " + target.getProcessTime().toMillis());

        String finalUrl = HttpUrl
                .parse(Constants.TARGETDONE)
                .newBuilder()
                .addQueryParameter(Constants.TARGETNAME,target.getName())
                .addQueryParameter(Constants.TASKNAME,targetsToTaskName.get(target))
                .build()
                .toString();
        System.out.println("finalUrl " + finalUrl);

        Platform.runLater(()->{
            HttpClientUtil.runAsync(finalUrl, new SimpleCallBack((tasksJson) -> {
            }));});

        getMoreTargets();
    }

    public synchronized void getMoreTargets(){
        if(targetsDone.get() > 0) {
            String finalUrl = HttpUrl
                    .parse(Constants.GET_TARGETS)
                    .newBuilder()
                    .addQueryParameter(Constants.TARGETS, String.valueOf(targetsDone.getAndSet(0)))
                    .build()
                    .toString();
            System.out.println("finalUrl " + finalUrl);

            Platform.runLater(() -> {
                HttpClientUtil.runAsync(finalUrl, new SimpleCallBack((tasksJson) -> {
                }));
            });
        }
    }

    public synchronized void updateProgress() {

    }
}
