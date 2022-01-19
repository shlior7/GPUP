import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class Compilation extends Task {
    private final String outFolder;
    private String javaFilesPath;

    public Compilation(String outFolder) {
        this.outFolder = outFolder;
        if (!new File(outFolder).mkdir())
            System.out.println("Already exists");
    }

    public String getName() {
        return "compilation";
    }

    public void setTarget(Target target) {
        this.targetToRunOn = target;
        this.javaFilesPath = String.join("/", target.getUserData().split("\\.")) + ".java";
    }

    @Override
    public void run() {
        int exitCode = -1;
        Instant before = Instant.now();
        targetToRunOn.setStatus(Status.IN_PROCESS);
        System.out.println("before running on " + targetToRunOn.name);
        try {
            Process process = new ProcessBuilder("javac", "-d", outFolder, "-cp", outFolder, javaFilesPath).start();
            exitCode = process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("ran on " + targetToRunOn.name);
        Instant after = Instant.now();
        targetToRunOn.setProcessTime(Duration.between(before, after));
        targetToRunOn.setResult(exitCode == 0 ? Result.Success : Result.Failure);
        onFinished.accept(targetToRunOn);
    }

}
