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

    public static void log(String Data, String targetName) throws IOException {
        print(Data);
        Logger.log(Data, targetName);
    }
}
