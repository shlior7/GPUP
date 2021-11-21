import java.util.*;
import java.io.IOException;


///////enum for choose

public class UI {
    private Logic logic;

    public UI(){
        logic = new Logic();
    }
    public static void print(String text) {
        System.out.println(text);
    }

    public void load() { ////////////////////////////////union with get target
        System.out.println("Please Enter the xml path");
        Scanner scanner = new Scanner(System.in);
        String path = scanner.nextLine();
    }

    public void mainOptions(){
        System.out.println("Hello user! This is the Generic Platform for Utilizing Processes!");
        System.out.println("Please select one of the following options:");
        List<String> arrayOptions = mainOptionsStrings();
        int numOfOption = 1;
        for (String option:arrayOptions) {
            System.out.println(numOfOption + ".) " + option);
            numOfOption++;
        }
        int choose = GetNumInRange(arrayOptions.size());
        while (choose!=8)
        switch (choose){
            case 1:
                loadXmlFile();
                break;
            case 2:
                if(!checkIfGraphNull()) {
                    logic.getLastTargetGraph().toString();
                }
                break;
            case 3:
                if(!checkIfGraphNull()) {
                    String target = getStringFromUser("Please enter the target name.");
                    ///////check if in graph
                }
                break;
            case 4:
                if(!checkIfGraphNull()) {
                    String source = getStringFromUser("Please enter the source target name.");
                    //////check if in graph
                    String dest = getStringFromUser("Please enter the destination target name.");
                    //////check if in graph
                    //find route
                }
                break;
            case 5:
                if(!checkIfGraphNull()){
                    //lastTargetGraph.runTask();
                }
                break;
            case 6:
                if(!checkIfGraphNull()) {
                    String target = getStringFromUser("Please enter the target name.");
                    ///////check if in graph
                    ///check if he is on a circuit
                }
                break;
            case 7:
                if(!checkIfGraphNull()){
                    //save to file
                }
                break;
            case 8:
                //load a file
                break;
            case 9:
                System.exit(1);
                break;
            default:
                /////////////????
                break;
        }

    }

    public String getStringFromUser(String textToPrint){
        System.out.println(textToPrint);
        Scanner scanner = new Scanner(System.in);
        String strFromUser = scanner.nextLine();
        return strFromUser;
    }



    public boolean checkIfGraphNull(){
        boolean graphIsNull = true;
        if(lastTargetGraph != null){
            graphIsNull=!graphIsNull;
        }
        else{
            System.out.println("Please load a valid target graph!");
        }
        return graphIsNull;
    }

    public int GetNumInRange(int lim) {
        Scanner s = new Scanner(System.in);
        try {
            int choose = s.nextInt();
            if(choose>=1 && choose<=lim) {
                return choose;
            }
            else{
                System.out.println("Please enter a number in the range of options!");
                return GetNumInRange(lim);
            }
        }
        catch (InputMismatchException e){
            System.out.println("Please enter a number!");
            return GetNumInRange(lim);
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

    public void loadXmlFile(){
        if(!validXML()){
            return;
        }
        TargetGraph targetGraph = new TargetGraph();
        Logic logic = new Logic();
        logic.Load("ex1-cycle.xml");
        System.out.println(logic.getLastTargetGraph());
    }

    public String getValidXML(){
        System.out.println("Please Enter the xml path");
        Scanner scanner = new Scanner(System.in);
        String xmlPath = scanner.nextLine();
        return xmlPath;
    }

    public boolean validXML(){
        return true;
    }
  
    public static void log(String Data, String targetName) throws IOException {
        print(Data);
        Logger.log(Data, targetName);

    }

    public <T,G> void printMap(Map<T,G> pairs){
        for (Map.Entry<T,G> entry : pairs.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}
