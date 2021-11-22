import java.io.IOException;
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
        this.successProbability = 0f;
        this.successWithWarningProbability = 0f;
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
    public Result run(Target target) throws InterruptedException, IOException {
        Random rand = new Random();
        UI.log("Start Time: " + DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()), target.name);
        UI.log("Start Task On " + target.name, target.name);
        UI.log("Targets Data: " + target.getUserData(), target.name);
        sleep(isRandom ? rand.nextInt(timeToProcess) : timeToProcess);
        Result result = getResult(rand);
        if (target.name.equals("L") || target.name.equals("M") || target.name.equals("K"))
            result = Result.Success;
        UI.log("Finished Time: " + DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()), target.name);
        UI.log("Task Finished with " + result.toString(), target.name);
        UI.print("--------------------------------\n");
        return result;
    }

    public Result getResult(Random rand) {
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
