package utils;

import com.google.gson.Gson;

public class Constants {
    public static final String USERNAME = "username";
    public static final String ROLE = "role";
    public static final String USER_NAME_ERROR = "username_error";


    public final static String USERS_JSON_FILE_PATH = "server/src/main/resources/json/users.json";
    // global constants
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");
    public final static String JHON_DOE = "<Anonymous>";
    public final static int REFRESH_RATE = 2000;
    public final static String CHAT_LINE_FORMATTING = "%tH:%tM:%tS | %.10s: %s%n";

    // Server resources locations
    public final static String BASE_DOMAIN = "localhost";
    private final static String FULL_SERVER_PATH = "http://" + BASE_DOMAIN + ":8080";
    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";

    public static final String SYMBOL = "symbol";

    public static final String CHAT_PARAMETER = "userstring";
    public static final String CHAT_VERSION_PARAMETER = "chatversion";

    public static final int INT_PARAMETER_ERROR = Integer.MIN_VALUE;
    
    public static final Gson GSON_INSTANCE = new Gson();


}
