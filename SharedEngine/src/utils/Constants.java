package utils;


import com.google.gson.Gson;

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
    public static final String GET_TASK_ALL = FULL_SERVER_PATH + "/task/all";
    public static final String TASK_SIGN = FULL_SERVER_PATH+ "/task/sign";
    public static final String TARGETDONE = FULL_SERVER_PATH+ "/task/target/done";
    public static final String GET_TARGETS = FULL_SERVER_PATH+ "/task/target/get";;












    public final static Gson GSON_INSTANCE = new Gson();

    // Server resources locations

    public static final String SYMBOL = "symbol";

    public static final String CHAT_PARAMETER = "userstring";
    public static final String CHAT_VERSION_PARAMETER = "chatversion";

    public static final int INT_PARAMETER_ERROR = Integer.MIN_VALUE;


}
