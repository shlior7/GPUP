package servlets;

import managers.UserManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Constants;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

import static utils.Constants.*;

@WebServlet(name = "Login", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

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
            throws ServletException, IOException {
        try {
            response.setContentType("text/plain;charset=UTF-8");
            String usernameFromSession = SessionUtils.getUsername(request);
            UserManager userManager = ServletUtils.getUserManager(getServletContext());

            if (usernameFromSession == null) {
                String usernameFromParameter = request.getParameter(USERNAME);
                String roleFromParameter = request.getParameter(ROLE);

                if (usernameFromParameter == null || usernameFromParameter.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request: userName is null or empty");
                    return;
                }
                if (!roleFromParameter.equals("Admin") && !roleFromParameter.equals("Worker")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request: roll needs to be `Worker` or `Admin` ");
                    return;
                }
                usernameFromParameter = usernameFromParameter.trim();
                synchronized (this) {
                    if (userManager.isUserExists(usernameFromParameter)) {
                        String errorMessage = "Username " + usernameFromParameter + " already exists. Please enter a different username.";
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, errorMessage);
                    } else {
                        if (roleFromParameter.equals("Worker")) {
                            String threadsFromParameter = request.getParameter(THREADS);
                            if (!validateThreads(threadsFromParameter)) {
                                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request: threads needs to be a number between 1-5");
                                return;
                            }
                            userManager.addWorker(usernameFromParameter, Integer.parseInt(threadsFromParameter));
                        } else {
                            userManager.addAdmin(usernameFromParameter);
                        }
                        request.getSession(true).setAttribute(Constants.USERNAME, usernameFromParameter);
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateThreads(String threadsString) {
        try {
            int threads = Integer.parseInt(threadsString);
            if (threads > 5 || threads < 1)
                return false;
        } catch (Exception e) {
            return false;
        }
        return true;
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
