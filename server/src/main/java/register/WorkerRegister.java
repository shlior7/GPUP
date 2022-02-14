package register;

import data.UserManager;
import express.ExpressRouter;
import express.http.HttpRequestHandler;
import express.utils.Status;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import utils.Constants;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import java.net.URI;

public class WorkerRegister {
    UserManager workerUserManager;
    JSONArray workersJson;

    public WorkerRegister() throws IOException {
        workerUserManager = new UserManager();
        workersJson = parseJSONFile("json/users.json");
        System.out.println(workersJson.toString());
    }

    HttpRequestHandler register = (req, res) -> {
        try {
            System.out.println(req.getBody());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(req.getBody()));
            System.out.println("b " + bufferedReader);
            JSONObject json = new JSONObject(new JSONTokener(bufferedReader));
            System.out.println("j " + json);
            String name = (String) json.get("userName");

            if (IntStream.range(0, workersJson.length()).anyMatch(n -> workersJson.getJSONObject(n).get("userName").equals(name))) {
                res.setStatus(Status._409);
                res.send("Worker Already Exists");
                return;
            }

            System.out.println("after checking");
            workersJson.put(json);
            saveToFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Reg");
        res.setStatus(Status._200);
        res.send("Registered");
    };

    public JSONArray parseJSONFile(String filename) throws JSONException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        String jsonTxt = IOUtils.toString(is);
        return new JSONArray(jsonTxt);
    }

    public synchronized void saveToFile() {
        Path file = Paths.get(Constants.USERS_JSON_FILE_PATH);
//        Path file = Paths.get(URI.create(this.getClass().getResource("/json/users.json").toString()));
        System.out.println(file.toAbsolutePath());
        System.out.println(workersJson.toString());
        try (AsynchronousFileChannel asyncFile = AsynchronousFileChannel.open(file,
                StandardOpenOption.WRITE)) {
            Future<FileLock> featureLock = asyncFile.lock();
            FileLock lock = featureLock.get();
            if (lock.isValid()) {
                Future<Integer> featureWrite = asyncFile.write(
                        ByteBuffer.wrap(workersJson.toString(2).getBytes()), 0);
                int written = featureWrite.get();
                lock.release();
                System.out.println(written);
            }
//            ByteBuffer byteBuffer = ByteBuffer.wrap(workersJson.toString().getBytes());
//            byteBuffer.flip();
//            asyncFile.write(byteBuffer, 0, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {
//                @Override
//                public void completed(Integer result, ByteBuffer attachment) {
//                    System.out.println("wrote json");
//                }
//
//                @Override
//                public void failed(Throwable exc, ByteBuffer attachment) {
//                    System.out.println("Failed json");
//                }
//            });
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ExpressRouter router = new ExpressRouter() {{
        get("/login", (req, res) -> res.send("Login page"));
        post("/register", register);
    }};
}
