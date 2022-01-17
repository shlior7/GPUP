import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Queue;

class RunTask_Option implements Option {
    @Override
    public String getText() {
        return "Run task on the target graph";
    }

    @Override
    public void actOption() {
        int timeToProcess = UI.promptInt("Please enter the time to process the simulation (milliseconds) ", 0, Integer.MAX_VALUE);
        boolean isRandom = UI.promptBoolean("Please enter if the process time should be random or not\n(the process time enter before is the maximum)");
        float successProbability = UI.promptFloat("Please enter the probability of success");
        float successWithWarningProbability = UI.promptFloat("Please enter the probability of warning given it was successful");
        boolean startFromLastPoint = Engine.validateGraph() && UI.promptBoolean("Do you want to start the task on the graph from the last point");

        Simulation simulation = new Simulation(timeToProcess, isRandom, successProbability, successWithWarningProbability);

        if (startFromLastPoint && !Engine.taskAlreadyRan())
            UI.warning("the graph does not have previous task runs");

        FileHandler.createLogLibrary(simulation.getName());

        Queue<Target> queue = Engine.InitTaskAndGetRunningQueue(startFromLastPoint);

        while (!queue.isEmpty()) {
            Target target = queue.poll();
            try {
                UI.log("Start Time: " + DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()), target.name);
                UI.log("Start Task On " + target.name, target.name);
                UI.log("Targets Data: " + target.getUserData(), target.name);
//                Engine.runTaskOnTarget(target, simulation);
                UI.log("Finished Time: " + DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()), target.name);
                UI.log("Task Finished with " + target.getResult().toString(), target.name);
                UI.println("--------------------------------\n");
            } catch (IOException e) {
                UI.warning("couldn't log to file");
            }
//            catch (InterruptedException ignored){}

        }
        Engine.setAllFrozensToSkipped();

        Engine.getStatusesStatistics().forEach((k, v) -> UI.printDivide(k + ": " + v.size() + " : {" + String.join(", ", v) + "}" + "\n"));
    }
}
