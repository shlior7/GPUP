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
    private final static String FULL_SERVER_PATH = "http://" + BASE_DOMAIN + ":8080";
    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";

    public final static Gson GSON_INSTANCE = new Gson();

}
