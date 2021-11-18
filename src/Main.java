import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) throws InterruptedException {
//        Logic logic = new Logic();
//        logic.Load("ex1-cycle.xml");
        int counter = 0;
        Target t1 = new Target("a", 1);
        Simulation simulation = new Simulation();
        try {
            System.out.println("start");
//                s = t1.run(simulation);
            while (counter < 10) {
                counter++;
                System.out.println("counter = " + counter);
                Thread.sleep(500);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        String completableFuture = CompletableFuture.supplyAsync(() -> {
//            String s = "fail";
//            try {
//                System.out.println("start");
////                s = t1.run(simulation);
//                while (counter.get() < 10) {
//                    counter.getAndIncrement();
//                    System.out.println("counter = " + counter);
//                    Thread.sleep(500);
//
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return s;
//        }).join();

        System.out.println("a: ");

//        while (!completableFuture.isDone()) {
//        }
//        completableFuture.join();
//        completableFuture.((a, b) -> {
//            System.out.println("a: " + a);
//        });
//        completableFuture.whenComplete((a, b) -> {
//            b.printStackTrace();
//            System.out.println((a));
//        });
//        System.out.println(logic.targetGraph);
    }

}

