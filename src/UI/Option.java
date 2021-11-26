package UI;

import Logic.Logic;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.LinkedList;

public interface Option {

    public String getText();

    public void actOption();

}

class Load_Option implements Option {

    @Override
    public String getText() {
        return "Load the `Logic.Target Graph`";
    }

    @Override
    public void actOption() {
        try {
            Logic.load(UI.prompt("Please enter the graphs XML full path that you want to load\n(A graph that was saved is acceptable as well)"));
        } catch (IOException e) {
            UI.error("with loading file : " + e.getMessage());
        } catch (ParserConfigurationException e) {
            UI.error("with parsing file : " + e.getMessage());
        } catch (SAXException e) {
            UI.error("with xml file : " + e.getMessage());
        } catch (Exception e) {
            UI.error(e.toString());
        }
    }
}

class GraphInfo_Option implements Option {
    @Override
    public String getText() {
        return "Displays general information on the target graph";
    }

    @Override
    public void actOption() {
        if(!Logic.validateGraph()) {
            UI.error("no target graph found");
            return;
        }
        UI.printDivide(Logic.graphInfo());
        UI.println(Logic.getPostTaskRunInfo());
        Logic.getStatusesStatistics().forEach((k, v) -> UI.printDivide(k + ": " + v.size() + " : {" + String.join(", ",v) + "}" +"\n"));

    }
}

class TargetInfo_Option implements Option {
    @Override
    public String getText() {
        return "Displays target information";
    }

    @Override
    public void actOption() {
        if(!Logic.validateGraph()) {
            UI.error("no target graph found");
            return;
        }
        UI.printDivide(Logic.targetInfo(UI.prompt("please enter the targets name")));
    }
}

class FindPath_Option implements Option {
    @Override
    public String getText() {
        return "Find path between two targets";
    }

    @Override
    public void actOption() {
        String target1 = UI.prompt("Please enter the `First` targets name",Logic::NoSuchTarget,"No such target in the graph");
        String target2 = UI.prompt("Please enter the `Second` targets name",Logic::NoSuchTarget,"No such target in the graph");
        String dependOrRequired = UI.prompt("Please enter if `Depends` or `Required` relation", "depends", "required");

        Logic.findPath(target1, target2, dependOrRequired.equals("depends")).forEach(UI::printPath);
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
        boolean isRandom = UI.promptBoolean("Please enter if the process time should be random or not\n(the process time enter before is the maximum)");
        float successProbability = UI.promptFloat("Please enter the probability of success");
        float successWithWarningProbability = UI.promptFloat("Please enter the probability of warning given it was successful");
        boolean startFromLastPoint = Logic.validateGraph() && UI.promptBoolean("Do you want to start the task on the graph from the last point");

        Simulation simulation = new Simulation(timeToProcess, isRandom, successProbability, successWithWarningProbability);
        try {
            if(!Logic.taskAlreadyRan())
                UI.warning("the graph does not have previous task runs");

            Logic.runTaskOnTargets(simulation, startFromLastPoint);

            UI.printDivide(Logic.getPostTaskRunInfo());
            Logic.getStatusesStatistics().forEach((k, v) -> UI.printDivide(k + ": " + v.size() + " : {" + String.join(", ",v) + "}" +"\n"));

        }
        catch (IOException e){
            UI.warning("task run but couldn't write log to file");
        }
        catch (InterruptedException ignored){
        }
    }
}

class FindCircuit_Option implements Option {
    @Override
    public String getText() {
        return "Find out if a target is in a circuit in the graph";
    }

    @Override
    public void actOption() {
        String targetName = UI.prompt("Please enter the targets name",Logic::NoSuchTarget,"No such target in the graph");
        LinkedList<String> circuit = Logic.findCircuit(targetName);
        if(circuit.size() == 0) {
            UI.printDivide("the target " + targetName + " was not found to be in a circuit");
            return;
        }
        UI.printPath(circuit);
    }
}

class SaveFile_Option implements Option {
    @Override
    public String getText() {
        return "Save current graph";
    }

    @Override
    public void actOption() {
        String xmlPath = UI.prompt("Please enter the the path which to save the graph file\n(no need for file suffix .xml/.json/.txt etc)");
        try{
            Logic.save(xmlPath);
        }
        catch (TransformerException e){
            UI.error(e.getMessage());
        }
    }
}
