import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException {
        Logic logic = new Logic();
        logic.Load("ex1-big.xml");
        Simulation simulation = new Simulation();
        logic.runTaskOnTargets(simulation);
    }

}

