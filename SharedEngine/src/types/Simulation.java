package types;

import TargetGraph.Result;
import TargetGraph.Status;
import TargetGraph.Target;

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

    public Simulation(String taskName, int timeToProcess, boolean isRandom, float successProbability, float successWithWarningProbability) {
        super(taskName, Simulation.class);
        this.timeToProcess = timeToProcess;
        this.isRandom = isRandom;
        this.successProbability = successProbability;
        this.successWithWarningProbability = successWithWarningProbability;
    }

    public Simulation(Simulation simulation) {
        super(simulation.getTaskName(), Simulation.class);
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
    public String getName() {
        return "Simulation";
    }

    @Override
    public void run() {
        Random rand = new Random();
        int timeToSleep = isRandom ? rand.nextInt(timeToProcess) : timeToProcess;
        Instant before = Instant.now();
        targetToRunOn.setStatus(Status.IN_PROCESS);
        targetToRunOn.setStartedTime(before);
        outputText.accept(targetToRunOn.name + " going to sleep for " + timeToSleep);
        try {
            outputText.accept("before " + targetToRunOn.name + " going to sleep");
            sleep(timeToSleep);
            outputText.accept("after " + targetToRunOn.name + " went to sleep");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Instant after = Instant.now();
        targetToRunOn.setProcessTime(Duration.between(before, after));
        targetToRunOn.setResult(getResult(rand));
    }
}
