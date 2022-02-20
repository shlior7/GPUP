package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "GreeterServlet", urlPatterns = "/hello")
public class GreeterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // fetch (query) parameter from the request

        // parameters are case sensitive.
        // The below will return null...
        // String name = req.getParameter("name");

        // response header content-type can hint the client initiating this request regarding the nature of the response...
        resp.setContentType("text/plain");
        resp.getWriter().println("Hello Lior");
    }
}
