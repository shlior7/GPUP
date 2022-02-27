package utils;


import com.google.gson.Gson;
import org.hildan.fxgson.FxGson;

public class Constants {

    // global constants
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");
    public final static String JHON_DOE = "<Anonymous>";
    public final static int REFRESH_RATE = 2000;
    public final static String CHAT_LINE_FORMATTING = "%tH:%tM:%tS | %.10s: %s%n";
    public static final String CONTEXT_PATH = "/app";

    public static final String USERNAME = "username";
    public static final String ROLE = "role";
    public static final String GRAPHNAME = "graphName";
    public static final String FROM_SCRATCH = "fromScratch";
    public static final String TASKNAME = "taskName";
    public static final String TARGETNAME = "targetName";
    public static final String THREADS = "threads";
    public static final String TARGETS = "targets";
    public static final String TASKOUTPUT = "taskOutput";
    public static final String RESULT = "result";

    // Server resources locations
    public final static String BASE_DOMAIN = "localhost";
    public static final String SIGNTO = "signUp";
    private final static String FULL_SERVER_PATH = "http://" + BASE_DOMAIN + ":8080" + CONTEXT_PATH;
    public final static String LOGIN_PATH = FULL_SERVER_PATH + "/login";
    public final static String UPLOAD_XML_PATH = FULL_SERVER_PATH + "/upload";
    public final static String GET_GRAPHS_ALL = FULL_SERVER_PATH + "/graphs/all";
    public static final String GET_USERS_ALL = FULL_SERVER_PATH + "/users/all";
    public static final String GET_GRAPH = FULL_SERVER_PATH + "/graphs/one";
    public static final String TASK_UPLOAD = FULL_SERVER_PATH + "/task/upload";
    public static final String VALIDATE_TASK = FULL_SERVER_PATH + "/task/validate";
    public static final String GET_TASK_ALL = FULL_SERVER_PATH + "/task/all";
    public static final String TASK_SIGN = FULL_SERVER_PATH + "/task/sign";
    public static final String TARGET_DONE_URL = FULL_SERVER_PATH + "/task/target/done";
    public static final String GET_TARGETS = FULL_SERVER_PATH + "/task/target/get";
    public static final String UPDATE_PROGRESS_POST_URL = FULL_SERVER_PATH + "/task/update/post";
    public static final String UPDATE_PROGRESS_GET_URL = FULL_SERVER_PATH + "/task/update/get";
    public static final String UPDATE_TARGET_LOGS_URL = FULL_SERVER_PATH + "/task/update/log";
    public static final String TASK_STOP_URL = FULL_SERVER_PATH + "/task/stop";
    public static final String TASK_PAUSE_URL = FULL_SERVER_PATH + "/task/pause";
    public static final String TASK_RESUME_URL = FULL_SERVER_PATH + "/task/resume";


    public final static String SEND_CHAT_LINE = FULL_SERVER_PATH + "/chat/send";
    public final static String CHAT_LINES_LIST = FULL_SERVER_PATH + "/chat/get";


    public final static Gson GSON_INSTANCE = FxGson.coreBuilder().disableHtmlEscaping().create();
//    public final static Gson GSON_INSTANCE = new Gson();

    // Server resources locations
    public static final String SYMBOL = "symbol";
    public static final String CHAT_PARAMETER = "userstring";
    public static final String CHAT_VERSION_PARAMETER = "chatversion";

    public static final int INT_PARAMETER_ERROR = Integer.MIN_VALUE;


}
