import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {
    static String libPath;

    public static void log(String Data, String targetName) throws IOException {
        String filePath = libPath + "/" + targetName + ".log";
        new File(filePath).createNewFile();
        FileWriter fw = new FileWriter(filePath);
        fw.write(Data);
        fw.close();
    }
}
