package types;

import TargetGraph.Result;
import TargetGraph.Status;
import TargetGraph.Target;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.StringJoiner;

public class Compilation extends Task {
    private final String outFolder;
    private final String workingDir;
    private String javaFilesPath;

    public Compilation(String outFolder, String workingDir) {
        this.workingDir = workingDir;
        this.outFolder = outFolder;
        new File(outFolder).mkdir();

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
        targetToRunOn.setStartedTime(before);
        outputText.accept("before running on " + targetToRunOn.name + " in " + workingDir + "/" + javaFilesPath);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("javac", "-d", outFolder, "-cp", outFolder, workingDir + "/" + javaFilesPath);
            Process process = processBuilder.start();
            outputText.accept("running the command `" + processBuilder.command().toString() + "`");
            exitCode = process.waitFor();

            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringJoiner sj = new StringJoiner(System.getProperty("line.separator"));
            reader.lines().iterator().forEachRemaining(sj::add);
            outputText.accept(sj.toString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        Instant after = Instant.now();
        targetToRunOn.setProcessTime(Duration.between(before, after));
        targetToRunOn.setResult(exitCode == 0 ? Result.Success : Result.Failure);
        onFinished.accept(targetToRunOn);
    }

}
