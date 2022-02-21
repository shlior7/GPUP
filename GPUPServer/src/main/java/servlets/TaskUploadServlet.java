package servlets;

import TargetGraph.Target;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.UserManager;
import types.Admin;
import types.Task;
import utils.Constants;
import utils.ServletUtils;
import utils.SessionUtils;
import types.Worker;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static utils.Constants.*;

@WebServlet(name = "TaskUploadServlet", urlPatterns = {"/task/upload"})
public class TaskUploadServlet extends HttpServlet {

    // urls that starts with forward slash '/' are considered absolute
    // urls that doesn't start with forward slash '/' are considered relative to the place where this servlet request comes from
    // you can use absolute paths, but then you need to build them from scratch, starting from the context path
    // ( can be fetched from request.getContextPath() ) and then the 'absolute' path from it.
    // Each method with it's pros and cons...

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        try (PrintWriter out = response.getWriter()) {
            response.setContentType("text/plain;charset=UTF-8");
            String usernameFromSession = SessionUtils.getUsername(request);
            UserManager userManager = ServletUtils.getUserManager(getServletContext());
            Admin admin = userManager.getAdmin(usernameFromSession);
            if (admin != null) {
                synchronized (this) {
                    String graphName = request.getParameter(GRAPHNAME);
//                    boolean fromScratch = Boolean.parseBoolean(request.getParameter(FROM_SCRATCH));
                    String requestData = request.getReader().lines().collect(Collectors.joining());
                    System.out.println("requestData = " + requestData);
                    JsonObject json = GSON_INSTANCE.fromJson(requestData, JsonObject.class);
                    JsonObject taskJson = json.get("task").getAsJsonObject();
                    Task task = GSON_INSTANCE.fromJson(requestData, (Class<? extends Task>) Class.forName(taskJson.get("type").getAsString()));

                    JsonObject targetsJson = json.get("targets").getAsJsonObject();
                    Set<Target> targets = Arrays.stream(GSON_INSTANCE.fromJson(targetsJson, Target[].class)).collect(Collectors.toSet());


                    ServletUtils.getEngine(getServletContext()).addTask(task, graphName, admin, targets);

                    response.setStatus(HttpServletResponse.SC_OK);
                }
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
