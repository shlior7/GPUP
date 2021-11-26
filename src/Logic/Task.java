package Logic;

import java.io.IOException;


public interface Task {
    String getName();
    Result run(Target target) throws InterruptedException, IOException;
}
