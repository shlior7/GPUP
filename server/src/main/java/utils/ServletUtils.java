package utils;

import Users.UserManager;
import chat.ChatManager;
import engine.Engine;
import engine.IEngine;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import static utils.Constants.INT_PARAMETER_ERROR;


public class ServletUtils {

    private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
    private static final String ENGINE_ATTRIBUTE_NAME = "engine";
    private static final String CHAT_MANAGER_ATTRIBUTE_NAME = "chatManager";

    /*
    Note how the synchronization is done only on the question and\or creation of the relevant managers and once they exists -
    the actual fetch of them is remained un-synchronized for performance POV
     */
    private static final Object userManagerLock = new Object();
    private static final Object chatManagerLock = new Object();

    public static IEngine getEngine(ServletContext servletContext) {
        synchronized (userManagerLock) {
            if (servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME) == null) {
                IEngine engine = new Engine();
                servletContext.setAttribute(ENGINE_ATTRIBUTE_NAME, engine);
            }
        }
        return (IEngine) (servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME));
    }

    public static UserManager getUserManager(ServletContext servletContext) {
        return getEngine(servletContext).getUserManager();
    }

    public static ChatManager getChatManager(ServletContext servletContext) {
        synchronized (chatManagerLock) {
            if (servletContext.getAttribute(CHAT_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(CHAT_MANAGER_ATTRIBUTE_NAME, new ChatManager());
            }
        }
        return (ChatManager) servletContext.getAttribute(CHAT_MANAGER_ATTRIBUTE_NAME);
    }

    public static int getIntParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException numberFormatException) {
            }
        }
        return INT_PARAMETER_ERROR;
    }
}
