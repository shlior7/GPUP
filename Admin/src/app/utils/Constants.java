package app.utils;


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

    // Server resources locations
    public final static String BASE_DOMAIN = "localhost";
    private final static String FULL_SERVER_PATH = "http://" + BASE_DOMAIN + ":8080" + CONTEXT_PATH;
    public final static String LOGIN_PATH = FULL_SERVER_PATH + "/login";
    public final static String UPLOAD_XML_PATH = FULL_SERVER_PATH + "/upload";
    public final static String GET_GRAPHS_ALL = FULL_SERVER_PATH + "/graphs/all";
    public static final String GET_USERS_ALL = FULL_SERVER_PATH + "/users/all";

    public final static Gson GSON_INSTANCE = new Gson();

}
