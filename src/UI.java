import java.util.*;
import java.io.IOException;

public class UI {
    public static void printDivider() {
        System.out.println("------------------");
    }

    public static void printDivide(String text) {
        printDivider();
        System.out.println(text);
    }

    public static void println(String text) {
        System.out.println(text);
    }

    public static void print(String text) {
        System.out.print(text);
    }

    public static void error(String err) {
        printDivide("Error: " + err);
        printDivider();
    }

    public static void warning(String warn) {
        printDivide("Warning: " + warn);
        printDivider();
    }

    public static void log(String Data, String targetName) throws IOException {
        println(Data);
        FileHandler.log(Data, targetName);
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

    public static void printPath(List<String> PathList) {
        for (String u : PathList) {
            UI.print(u + "->");
        }
    }

    public static int promptInt(String question, int min, int max) {
        int input = 0;
        Scanner scanner = new Scanner(System.in);

        do {
            printDivide(question);
            if (scanner.hasNextInt())
                input = scanner.nextInt();
        } while (input < min || input > max);
        return input;
    }

    public static float promptFloat(String question, double min, double max) {
        float input = 0;
        Scanner scanner = new Scanner(System.in);

        do {
            printDivide(question);
            if (scanner.hasNextFloat())
                input = scanner.nextFloat();

        } while (input < min || input > max);

        return input;
    }

    public static float promptFloat(String question) {
        float input = 0;
        Scanner scanner = new Scanner(System.in);

        do {
            printDivide(question);

            if (scanner.hasNextFloat())
                input = scanner.nextFloat();


        } while (input < 0 || input > 1);

        return input;
    }

    public static boolean promptBoolean(String question) {
        String input = "";
        Scanner scanner = new Scanner(System.in);
        boolean no;
        boolean yes;

        do {
            printDivide(question + "(Y/N)");
            if (scanner.hasNext()) {
                input = scanner.nextLine();
            }
            yes = input.equalsIgnoreCase("y");
            no = input.equalsIgnoreCase("n");
        } while (!yes && !no);

        return yes;
    }

}
