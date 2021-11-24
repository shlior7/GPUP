import java.util.concurrent.Callable;
import java.util.function.Function;

public interface Option {

    public String getText();

    public void actOption();

}

class Load_Option implements Option {

    @Override
    public String getText() {
        return "Load the `Target Graph`";
    }

    @Override
    public void actOption() {
        Logic.load(UI.prompt("Please enter the graphs XML full path that you want to load"));
    }
}

class GraphInfo_Option implements Option {

    @Override
    public String getText() {
        return "Displays general information on the target graph";
    }

    @Override
    public void actOption() {
        Logic.graphInfo();
    }
}

class TargetInfo_Option implements Option {

    @Override
    public String getText() {
        return "Displays target information";
    }

    @Override
    public void actOption() {
        Logic.targetInfo(UI.prompt("please enter the targets name"));
    }
}

class FindPath_Option implements Option {
    @Override
    public String getText() {
        return "Find path between two targets";
    }

    @Override
    public void actOption() {
        String target1 = UI.prompt("Please enter the first targets name");
        String target2 = UI.prompt("Please enter the second targets name");
        String dependOrRequired = UI.prompt("Please enter if `Depends` or `Required` relation", "depends", "required");

        Logic.findPath(target1, target2, dependOrRequired.equals("depends"));
    }
}

class RunTask_Option implements Option {
    @Override
    public String getText() {
        return "Run task on the target graph";
    }

    @Override
    public void actOption() {
        int timeToProcess = UI.promptInt("Please enter the time to process the simulation (milliseconds) ", 0, Integer.MAX_VALUE);
        boolean isRandom = UI.promptBoolean("Please enter if the process time should be random or not(the process time enter before is the maximum)");
        float successProbability = UI.promptFloat("Please enter the probability of success");
        float successWithWarningProbability = UI.promptFloat("Please enter the probability of warning given it was successful");
        boolean startFromLastPoint = Logic.validateGraph() && UI.promptBoolean("Do you want to start the task on the graph from the last point");

        Simulation simulation = new Simulation(timeToProcess, isRandom, successProbability, successWithWarningProbability);
        Logic.runTaskOnTargets(simulation, startFromLastPoint);
    }
}

class FindCircle_Option implements Option {
    @Override
    public String getText() {
        return "Find out if a target is in a circle in the graph";
    }

    @Override
    public void actOption() {
        String targetName = UI.prompt("Please enter the targets name");

        Logic.findCircle(targetName);
    }
}
