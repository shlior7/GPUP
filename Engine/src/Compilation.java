import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.StringJoiner;

public class Compilation extends Task {
    private final String outFolder;
    private String javaFilesPath;
    private String workingDir;

    public Compilation(String outFolder, String workingDir) {
        this.workingDir = workingDir;
        this.outFolder = outFolder;
        if (!new File(outFolder).mkdir())
            System.out.println("Already exists");
    }

    public Compilation(Compilation task) {
        this.outFolder = task.outFolder;
        this.workingDir = task.workingDir;
    }

    public String getName() {
        return "compilation";
    }

    public void setTarget(Target target) {
        this.targetToRunOn = target;
        this.javaFilesPath = String.join("/", target.getUserData().split("\\.")) + ".java";
    }

    @Override
    public Task copy() {
        return new Compilation(this);
    }

    @Override
    public void run() {
        int exitCode = -1;
        Instant before = Instant.now();
        targetToRunOn.setStatus(Status.IN_PROCESS);
        outputText.accept("before running on " + targetToRunOn.name);
        System.out.println("before running on " + targetToRunOn.name);
        try {
            Process process = new ProcessBuilder("javac", "-d", outFolder, "-cp", outFolder, workingDir + "/" + javaFilesPath).start();
            exitCode = process.waitFor();

            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringJoiner sj = new StringJoiner(System.getProperty("line.separator"));
            reader.lines().iterator().forEachRemaining(sj::add);
            outputText.accept(sj.toString());
            System.out.println(sj);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        Instant after = Instant.now();
        targetToRunOn.setProcessTime(Duration.between(before, after));
        targetToRunOn.setResult(exitCode == 0 ? Result.Success : Result.Failure);
        onFinished.accept(targetToRunOn);
    }

}
