import java.io.*;

public class Logger {
    static String libPath;

    public static void log(String Data, String targetName) throws IOException {
        String filePath = libPath + "/" + targetName + ".log";
        new File(filePath).createNewFile();
        FileWriter fw = new FileWriter(filePath, true);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter out = new PrintWriter(bw);
        out.println(Data + "\n");
        out.close();
    }
}
