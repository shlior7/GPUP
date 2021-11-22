import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException, TransformerException {
        Logic logic = new Logic();
        logic.load("ex1-big.xml");

        Simulation simulation = new Simulation();
        logic.runTaskOnTargets(simulation);
        UI.print("wanna try again fucker?");
//        new Scanner(System.in).nextLine();
//        logic.runTaskOnTargetsLeft(simulation);
        logic.save("ex1_saved.xml");
    }

}

