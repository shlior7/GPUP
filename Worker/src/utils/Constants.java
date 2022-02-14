package utils;


import com.google.gson.Gson;

public class Constants {

    // global constants
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");
    public final static String JHON_DOE = "<Anonymous>";
    public final static int REFRESH_RATE = 2000;
    public final static String CHAT_LINE_FORMATTING = "%tH:%tM:%tS | %.10s: %s%n";

    // Server resources locations
    public final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/app";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    public final static String WORKERS_SERVER_PATH = FULL_SERVER_PATH + "/worker";
    public final static String REGISTER_PAGE = FULL_SERVER_PATH + "/register";
    public final static String WORKER_REGISTER_PAGE = WORKERS_SERVER_PATH + "/register";
    public final static String USERS_LIST = FULL_SERVER_PATH + "/userslist";
    public final static String LOGOUT = FULL_SERVER_PATH + "/chat/logout";
    public final static String SEND_CHAT_LINE = FULL_SERVER_PATH + "/pages/chatroom/sendChat";
    public final static String CHAT_LINES_LIST = FULL_SERVER_PATH + "/chat";

    public final static Gson GSON_INSTANCE = new Gson();

}
