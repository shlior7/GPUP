import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.function.Consumer;

import static java.lang.Thread.sleep;

public class Simulation extends Task {
    private final int timeToProcess;
    private final boolean isRandom;
    private final float successProbability;
    private final float successWithWarningProbability;

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

    public Result getResult(Random rand) {
        if (rand.nextFloat() <= successProbability) {
            if (rand.nextFloat() <= successWithWarningProbability)
                return Result.Warning;
            return Result.Success;
        }
        return Result.Failure;
    }

    @Override
    public Task copy() {
        return new Simulation(this);
    }

    @Override
    public void setTarget(Target targetToRunOn) {
        this.targetToRunOn = targetToRunOn;
    }

    @Override
    public void setFuncOnFinished(Consumer<Target> onFinished) {
        this.onFinished = onFinished;
    }

    @Override
    public String getName() {
        return "simulation";
    }

    @Override
    public void run() {
        Random rand = new Random();
        Instant before = Instant.now();
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
        onFinished.accept(targetToRunOn);
    }
}
