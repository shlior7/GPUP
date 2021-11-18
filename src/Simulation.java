import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import static java.lang.Thread.sleep;

public class Simulation implements Task {
    private int timeToProcess;
    private boolean isRandom;
    private float successProbability;
    private float successWithWarningProbability;

    public Simulation() {
        this.timeToProcess = 2000;
        this.isRandom = true;
        this.successProbability = 0.5f;
        this.successWithWarningProbability = 0.5f;
    }

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
    public Result run() throws InterruptedException {
        Random rand = new Random();
        UI.print(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()));
        UI.print("Before Sleep");
        sleep(isRandom ? rand.nextInt(timeToProcess) : timeToProcess);
        UI.print("After Sleep");

        if (rand.nextFloat() <= successProbability) {
            if (rand.nextFloat() <= successWithWarningProbability)
                return Result.Warning;
            return Result.Success;
        }
        return Result.Failure;
    }

    public int getTimeToProcess() {
        return timeToProcess;
    }

    public boolean isRandom() {
        return isRandom;
    }

    public float getSuccessProbability() {
        return successProbability;
    }

    public float getSuccessWithWarningProbability() {
        return successWithWarningProbability;
    }
}
