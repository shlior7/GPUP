package servlets;

import types.Admin;

import utils.ServletUtils;
import utils.SessionUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

@WebServlet(name = "XmlUploadServlet", urlPatterns = {"/upload"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class XmlUploadServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();

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
        try {
            ServletUtils.getEngine(getServletContext()).loadXmlFile(request.getInputStream(), uploadedBy);
            out.println("{\"message\": \"Uploaded file Succesfully\"}");
            response.setStatus(200);
        } catch (Exception exception) {
            out.println(exception.getMessage());
            response.setStatus(401);
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
        } catch (Exception exception) {
            exception.printStackTrace();
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
        } catch (Exception exception) {
            exception.printStackTrace();
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
