package servlets;

import TargetGraph.TargetGraph;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import types.GraphInfo;
import utils.ServletUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static utils.Constants.GSON_INSTANCE;

@WebServlet(name = "GraphListServlet", urlPatterns = {"/graphs/all"})
public class GraphListServlet extends HttpServlet {

    private List<GraphInfo> generateGraphInfoCollection(Collection<TargetGraph> graphCollection) {
        List<GraphInfo> graphInfoList = new ArrayList<>();
        graphCollection.forEach(g -> graphInfoList.add(new GraphInfo((g))));
        return graphInfoList;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Collection<TargetGraph> allGraphs = ServletUtils.getEngine(getServletContext()).getAllGraphs();
            String json = GSON_INSTANCE.toJson(generateGraphInfoCollection(allGraphs));
            System.out.println(json);
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
