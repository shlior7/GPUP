import java.util.Random;

import static java.lang.Thread.sleep;

public class Simulation implements Task {
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

    @Override
    public String getName() {
        return "Simulation";
    }

    @Override
    public Result run(Target target) throws InterruptedException {
        Random rand = new Random();
        sleep(isRandom ? rand.nextInt(timeToProcess) : timeToProcess);
        return getResult(rand);
    }

    public Result getResult(Random rand) {
        if (rand.nextFloat() <= successProbability) {
            if (rand.nextFloat() <= successWithWarningProbability)
                return Result.Warning;
            return Result.Success;
        }
        return Result.Failure;
    }
}
