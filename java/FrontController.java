package util;

import java.io.IOException;
import java.rmi.server.ServerCloneException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class FrontController extends HttpServlet {
    private List<Class<?>> listeController;

    public void init(ServletConfig conf) throws ServletException {
        super.init(conf);
        String packages = this.getInitParameter("package");
        listeController = allMappingUrls(packages);
    }

    public List<Class<?>> allMappingUrls(String packageName) {
        listeController = new ArrayList<>();
        ServletContext context = getServletContext();
        String path = "/WEB-INF/classes/" + packageName;

        Set<String> classNames = context.getResourcePaths(path);
        if (classNames != null) {
            for (String className : classNames) {
                if (className.endsWith(".class")) {
                    String fullClassName = className.substring(0, className.length() - 6);
                    int taille = fullClassName.split("/").length;
                    fullClassName = fullClassName.split("/")[taille - 2] + "." + fullClassName.split("/")[taille - 1];

                    try {
                        Class<?> clazz = Class.forName(fullClassName);
                        AnnotationController annotation = clazz.getAnnotation(AnnotationController.class);
                        if (annotation != null) {
                            System.out.println("value" + annotation.value());
                            listeController.add(clazz);
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
        } else {
            System.out.println("class null");
        }
        return listeController;
    }
      

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        resp.getWriter().println("<h1> Hello world!!</h1>");
        resp.getWriter().println("<h1>Lien :" + req.getRequestURI() + "</h1>");

        for (Class<?> controllerClass : listeController) {
            AnnotationController annotation = controllerClass.getAnnotation(AnnotationController.class);
            if (annotation != null) {
                String nameController = controllerClass.getSimpleName();
                resp.getWriter().println("controller :" + nameController);
            } else {
                resp.getWriter().println("Annotation nulle");
            }
        }
    }
}
