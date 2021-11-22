import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;
import java.io.IOException;
import java.util.Scanner;

public class UI {
    public static void print(String text) {
        System.out.println(text);
    }

    public void load() {
        System.out.println("Please Enter the xml path");
        Scanner scanner = new Scanner(System.in);
        String path = scanner.nextLine();
    }

    public void mainOptions() {
        System.out.println("Hello user! This is the Generic Platform for Utilizing Processes!");
        System.out.println("Please select one of the following options:");
        List<String> arrayOptions = mainOptionsStrings();
        int numOfOption = 1;
        for (String option : arrayOptions) {
            System.out.println(numOfOption + ".) " + option);
            numOfOption++;
        }

    }

    public int GetNumInRange(int lim) {
        Scanner s = new Scanner(System.in);
        try {
            int choose = s.nextInt();
            if (choose >= 1 && choose <= lim) {
                return choose;
            } else {
                System.out.println("Please enter a number in the range of options!");
                return GetNumInRange(lim);
            }
        } catch (InputMismatchException e) {
            System.out.println("Please enter a number!");
            return GetNumInRange(lim);
        }
    }

    public List<String> mainOptionsStrings() {
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

    public TargetGraph loadXmlFile() {
        if (!validXML()) {
            return null;
        }
        TargetGraph targetGraph = new TargetGraph();
        Logic logic = new Logic();
        logic.Load("ex1-cycle.xml");
        System.out.println(logic.targetGraph);
        return targetGraph;
    }

    public boolean validXML() {
        return true;
    }

    public static void log(String Data, String targetName) throws IOException {
        print(Data);
        Logger.log(Data, targetName);
    }
}
