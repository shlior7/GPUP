package servlets;

import TargetGraph.Target;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import task.TaskRunner;
import types.TaskStatus;
import types.Worker;
import utils.ServletUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static utils.Constants.*;

@WebServlet(name = "TaskGetUpdateServlet", urlPatterns = {"/task/update/get"})
public class TaskGetUpdateServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            String taskName = request.getParameter(TASKNAME);


            TaskRunner taskRunner = ServletUtils.getEngine(getServletContext()).getTaskManager().getTask(taskName);
            if (taskRunner == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, taskName + " no task like that found");
                return;
            }

            Set<Target> targets = taskRunner.getGraph().getCurrentTargets();
            Double progress = taskRunner.getProgress().get();
            TaskStatus taskStatus = taskRunner.getTaskData().getStatus();
            Map<Worker, List<Target>> workerListMap = taskRunner.getTaskData().getWorkerListMap();


            List<String> targetLogs = ServletUtils.getEngine(getServletContext()).getLogs(taskName);


            Map<String, Object> jsonMap = new HashMap() {{
                put("targets", GSON_INSTANCE.toJson(targets));
                put("progress", progress.toString());
                put("taskStatus", taskStatus.toString());
                put("targetLogs", targetLogs);
                put("workersTargets", workerListMap);
            }};

            out.println(jsonMap);
            out.flush();
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

