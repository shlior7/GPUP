import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

public class UI {
    public static void printDivider() {
        printDivider('-');
    }

    public static void printDivider(char divider) {
        for (int i = 0; i < 20; i++) {
            System.out.print(divider);
        }
        System.out.println();
    }

    public static void printDivide(String text) {
        if (!text.equals("")) {
            printDivider();
            System.out.println(text);
        }
    }

    public static void println(String text) {
        System.out.println(text);
    }

    public static void print(String text) {
        System.out.print(text);
    }

    public static void error(String err) {
        printDivider('*');
        println("Error: " + err);
        printDivider('*');

    }

    public static void warning(String warn) {
        printDivider('#');
        println("Warning: " + warn);
        printDivider('#');
    }

    public static void log(String Data, String targetName) throws IOException {
        println(Data);
//        try{
//        FileHandler.log(Data, targetName);
//        }
//        catch (IOException e){
//            UI.error(e.getMessage());
//        }
//        catch (Exception ignored){
//        }
    }


    public static String prompt(String question, Predicate<String> notRight, String errorMessage) {
        Scanner scanner = new Scanner(System.in);
        String input;
        boolean notOk;
        do {
            printDivide(question);
            input = scanner.nextLine();
            notOk = notRight.test(input);
            if (notOk)
                UI.error(input + " - " + errorMessage);
        } while (notOk);

        return input;
    }

    public static String prompt(String question, String... options) {
        String input;
        Scanner scanner = new Scanner(System.in);
        boolean inOptions = true;
        do {
            printDivide(question);
            input = scanner.nextLine();
            String finalInput = input;
            inOptions = Arrays.stream(options).anyMatch(option -> option.equalsIgnoreCase(finalInput)) || options.length == 0;
        } while (!inOptions);

        return input;
    }

    public static void printAllPaths(LinkedList<List<String>> allPaths) {
        if (allPaths.size() == 0) {
            UI.error("No directed path was found between the targets");
            return;
        }
        allPaths.forEach(UI::printPath);
    }

    public static void printPath(List<String> PathList) {
        for (String u : PathList) {
            UI.print(u + "->");
        }
        UI.println("");
    }

    public static int promptInt(String question, int min, int max) {
        int input = 0;
        Scanner scanner = new Scanner(System.in);

        do {
            printDivide(question);
            while (!scanner.hasNextInt()) {
                System.out.println("That's not a number!");
                scanner.next(); // this is important!
            }
            input = scanner.nextInt();
        } while (input < min || input > max);
        return input;
    }

    public static float promptFloat(String question, double min, double max) {
        float input = 0;
        Scanner scanner = new Scanner(System.in);

        do {
            printDivide(question);
            while (!scanner.hasNextFloat()) {
                System.out.println("That's not a number!");
                scanner.next();
            }
            input = scanner.nextFloat();

        } while (input < min || input > max);

        return input;
    }

    public static float promptFloat(String question) {
        float input = 0;
        Scanner scanner = new Scanner(System.in);

        do {
            printDivide(question);
            while (!scanner.hasNextFloat()) {
                System.out.println("That's not a number!");
                scanner.next();
            }
            input = scanner.nextFloat();

        } while (input < 0 || input > 1);

        return input;
    }

    public static boolean promptBoolean(String question) {
        Scanner scanner = new Scanner(System.in);
        printDivide(question + "(Y/N)");
        while (!scanner.hasNext("[ynYN]")) {
            UI.error("only Y/N is acceptable");
            scanner.next();
        }
        String input = scanner.next();
        return input.equalsIgnoreCase("y");
    }
}
