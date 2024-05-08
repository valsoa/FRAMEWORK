package util;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet{
    protected void doGet(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException{
        processRequest(req,rep);
    }
    
    protected void doPost(HttpServletRequest req,HttpServletResponse rep) throws ServletException,IOException{
        processRequest(req,rep);
    }
    protected void processRequest(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException{
        rep.setContentType("text/html");
        rep.getWriter().println("<h1> Hello world!!</h1>");
        rep.getWriter().println("<h1>Lien :"+req.getRequestURI()+"</h1>");
    }
    
}