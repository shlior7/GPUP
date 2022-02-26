package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import managers.UserManager;
import types.Task;
import types.Worker;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;
import java.io.PrintWriter;

import static utils.Constants.*;

@WebServlet(name = "SignToTaskServlet", urlPatterns = {"/task/sign"})
public class SignToTaskServlet extends HttpServlet {

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
            response.setContentType("application/json");
            String usernameFromSession = SessionUtils.getUsername(request);
            UserManager userManager = ServletUtils.getUserManager(getServletContext());
            Worker worker = userManager.getWorker(usernameFromSession);
            if (worker != null) {
                synchronized (this) {
                    String taskName = request.getParameter(TASKNAME);
                    boolean signTo = Boolean.parseBoolean(request.getParameter(SIGNTO));
                    Task task = ServletUtils.getEngine(getServletContext()).getTaskManager().signUserToTask(worker, taskName, signTo);
                    System.out.println("signed " + worker.getName() + " to " + taskName);
                    System.out.println("proof " + ServletUtils.getEngine(getServletContext()).getTaskManager().getTask(taskName).isWorkerRegisteredToThisTask(worker));
                    if (signTo)
                        out.println(GSON_INSTANCE.toJson(task));

                    out.flush();
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                out.println("Request is not from a worker");
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
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
