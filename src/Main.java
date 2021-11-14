import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        Logic logic = new Logic();
        logic.Load("ex1-cycle.xml");
        System.out.println(logic.targetGraph);
    }

}

