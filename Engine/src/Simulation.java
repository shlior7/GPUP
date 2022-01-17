import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.Thread.sleep;

public class Simulation implements Task {
    private final int timeToProcess;
    private final boolean isRandom;
    private final float successProbability;
    private final float successWithWarningProbability;
    private BiConsumer<Target, Task> onFinished = null;
    private Target targetToRunOn = null;

    public Simulation(int timeToProcess, boolean isRandom, float successProbability, float successWithWarningProbability) {
        this.timeToProcess = timeToProcess;
        this.isRandom = isRandom;
        this.successProbability = successProbability;
        this.successWithWarningProbability = successWithWarningProbability;
    }

    public Simulation(Simulation simulation) {
        this.timeToProcess = simulation.timeToProcess;
        this.isRandom = simulation.isRandom;
        this.successProbability = simulation.successProbability;
        this.successWithWarningProbability = simulation.successWithWarningProbability;
    }

    @Override
    public String getName() {
        return "{Simulation on " + targetToRunOn.name + "}";
    }

//    @Override
//    public void run(Target target) throws InterruptedException {
//        Instant before = Instant.now();
//        Random rand = new Random();
//        sleep(isRandom ? rand.nextInt(timeToProcess) : timeToProcess);
//        Instant after = Instant.now();
//        target.setProcessTime(Duration.between(before, after));
//        target.setResult(getResult(rand));
//    }

    public Result getResult(Random rand) {
        if (rand.nextFloat() <= successProbability) {
            if (rand.nextFloat() <= successWithWarningProbability)
                return Result.Warning;
            return Result.Success;
        }
        return Result.Failure;
    }

    @Override
    public void setTarget(Target targetToRunOn) {
        this.targetToRunOn = targetToRunOn;
    }

    @Override
    public void setFuncOnFinished(BiConsumer<Target, Task> onFinished) {
        this.onFinished = onFinished;
    }

    public synchronized void running(Random rand) {
        System.out.println("before running on " + targetToRunOn.name);
        try {
            sleep(isRandom ? rand.nextInt(timeToProcess) : timeToProcess);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("ran on " + targetToRunOn.name);
    }

    @Override
    public void run() {
        Instant before = Instant.now();
        Random rand = new Random();
        targetToRunOn.setStatus(Status.IN_PROCESS);
        System.out.println("before running on " + targetToRunOn.name);
        try {
            sleep(isRandom ? rand.nextInt(timeToProcess) : timeToProcess);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("ran on " + targetToRunOn.name);
        Instant after = Instant.now();
        targetToRunOn.setProcessTime(Duration.between(before, after));
        targetToRunOn.setResult(getResult(rand));
        onFinished.accept(targetToRunOn, this);
    }
}
