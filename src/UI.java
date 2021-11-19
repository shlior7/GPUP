import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class UI {
    public void load() {
        System.out.println("Please Enter the xml path");
        Scanner scanner = new Scanner(System.in);
        String path = scanner.nextLine();
    }

    public void mainOptions(){
        System.out.println("Hello user! This is the Generic Platform for Utilizing Processes!");
        System.out.println("Please select one of the following options:");
        List<String> arrayOptions =mainOptionsStrings();
        int numOfOption = 1;
        for (String option:arrayOptions) {
            System.out.println(numOfOption + ".) " + option);
            numOfOption++;
        }
    }


    public List<String> mainOptionsStrings(){
        List<String> arrayOptions = new LinkedList<>();
        arrayOptions.add("Please enter the XML full path that you want to load");
        arrayOptions.add("Displays general information on the target graph");
        arrayOptions.add("Displays target information");
        arrayOptions.add("Finding a route between 2 targets");
        arrayOptions.add("Run a task");
        arrayOptions.add("Check if a particular target is included in the circuit or not");
        arrayOptions.add("Saving the system status to a file");
        arrayOptions.add("Loading the system status from a file");
        arrayOptions.add("Exit");
        return arrayOptions;
    }

    public TargetGraph loadXmlFile(){
        if(!validXML()){
            return null;
        }
        TargetGraph targetGraph = new TargetGraph();
        Logic logic = new Logic();
        logic.Load("ex1-cycle.xml");
        System.out.println(logic.targetGraph);
        return targetGraph;
    }

    public boolean validXML(){
        return true;
    }
}
