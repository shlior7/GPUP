package servlets;

import TargetGraph.Target;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.UserManager;
import task.TaskRunner;
import types.Task;
import types.TaskStatus;
import types.Worker;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.stream.Collectors;

import static utils.Constants.GSON_INSTANCE;
import static utils.Constants.TASKNAME;

@WebServlet(name = "TaskGetUpdateServlet", urlPatterns = {"/task/update/get"})
public class TaskGetUpdateServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String usernameFromSession = SessionUtils.getUsername(request);
            UserManager userManager = ServletUtils.getUserManager(getServletContext());
            Worker worker = userManager.getWorker(usernameFromSession);
            if (worker != null) {
                String taskName = request.getParameter(TASKNAME);
                TaskRunner taskRunner = ServletUtils.getEngine(getServletContext()).getTaskManager().getTask(taskName);

                Set<Target> targets = taskRunner.getGraph().getCurrentTargets();
                String taskOutput = taskRunner.getTaskOutput().toString();
                Double progress = taskRunner.getProgress().get();
                TaskStatus taskStatus = taskRunner.getTaskData().getStatus();

            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                out.println("Request is not from a worker");
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
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
        processRequest(request, response);
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
        processRequest(request, response);
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
