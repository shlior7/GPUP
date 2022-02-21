package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import task.TaskRunner;
import types.*;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static utils.Constants.GSON_INSTANCE;

@WebServlet(name = "TaskListServlet", urlPatterns = {"/task/all"})
public class TaskListServlet extends HttpServlet {

    private List<TaskInfo> generateJSONFromTaskCollection(Collection<TaskRunner> taskCollection) {
        List<TaskInfo> tasksList = new ArrayList<>();
        taskCollection.forEach(taskRunner -> tasksList.add(new TaskInfo(taskRunner.getTaskData())));
        return tasksList;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            String usernameFromSession = SessionUtils.getUsername(request);
            if (usernameFromSession == null) {
                out.println("{\"message\": \"No User in session \"}");
                response.setStatus(401);
                return;
            }
            Admin uploadedBy = ServletUtils.getEngine(getServletContext()).getUserManager().getAdmin(usernameFromSession);
            if (uploadedBy == null) {
                out.println("{\"message\": \"User in session is not Admin \"}");
                response.setStatus(401);
                return;
            }
            Collection<TaskRunner> allTasks = ServletUtils.getEngine(getServletContext()).getTaskManager().getAllTasks();
            String json = GSON_INSTANCE.toJson(generateJSONFromTaskCollection(allTasks));
            System.out.println("response = " + json);
            out.println(json);
            out.flush();
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
