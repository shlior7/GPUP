import java.util.List;
import java.util.Scanner;

public class Menu {
    private final String title;
    private final List<Option> options;

    public Menu(
            final String title,
            final List<Option> options
    ) {
        this.title = title;
        this.options = options;
    }

    public void spawnMenu() {
        final Scanner sc = new Scanner(System.in);
        int choice = 0;

        UI.printDivide(this.title);

        do {
            UI.printDivider();
            for (int i = 0; i < this.options.size(); i++) {
                UI.println((i + 1) + " - " + this.options.get(i).getText());
            }
            UI.println(this.options.size() + 1 + " - Exit");

            choice = UI.promptInt("Please enter the action you would like to excecute", 0, this.options.size() + 1);

            if (choice > 0 && (choice - 1) < this.options.size())
                this.options.get(choice - 1).actOption();
            else if (choice == this.options.size() + 1) {
                UI.println("finito la comedia, adios!");
                return;
            }

        } while (true);
    }
}