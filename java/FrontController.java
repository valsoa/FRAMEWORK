package util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.http.HttpResponse;
import java.rmi.server.ServerCloneException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import util.Annotation.*;
import java.lang.annotation.Annotation;


public class FrontController extends HttpServlet {
    private List<Class<?>> listeController;
    private HashMap<String,Mapping> urlMapping = new HashMap<>();

    public void init(ServletConfig conf) throws ServletException {
        super.init(conf);
        String packages = this.getInitParameter("package");
        ServletContext context = getServletContext();
        listeController = Util.allMappingUrls(packages,util.Annotation.AnnotationController.class,context);
        urlMapping =Util.getUrlMapping(listeController);

    }                                                                                                                                                                                               
    public Object getValue(String methodName, String className){
        try{
            Class<?> clas =Class.forName(className);
            Method method =clas.getMethod(methodName);
            Object obj = clas.newInstance();
            return method.invoke(obj);
        }
        catch(Exception e){
            return null;
        }
    }
   
    public void sendModelView(ModelView model,HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException{
        for(Map.Entry<String,Object> entry : model.getData().entrySet()){
            req.setAttribute(entry.getKey(),entry.getValue());
        }
        RequestDispatcher dispatch = req.getRequestDispatcher(model.getUrl());
        dispatch.forward(req,rep);
    }  
    public void executeUrl(HttpServletRequest req, HttpServletResponse resp,boolean test) throws ServletException, IOException {
        resp.getWriter().println("<br>urlMapping:"+urlMapping);
        for (Map.Entry<String,Mapping> entry : urlMapping.entrySet()) {
            String url = entry.getKey();
            Mapping value = entry.getValue();
            if(url.equals(req.getRequestURI())){
                Object urlValue=getValue(value.getMethodName(),value.getClassName());
                resp.getWriter().println("<br>valeur:" + value.getClassName() +"_"+ value.getMethodName());
                if(urlValue instanceof String s){
                    resp.getWriter().println("<br>valeur methode:"+s);
                }
                else if(urlValue instanceof ModelView m){
                    sendModelView(m,req,resp);
                }
                else{
                    resp.getWriter().println("<br>type de retour non valide");
                }
                test=true;
                break;
            }    
        }
        if (!test) {
            throw new IllegalArgumentException("url innexistante");
        }
    }
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         boolean test = false;
        resp.setContentType("text/html");
        resp.getWriter().println("<h1> Hello world!!</h1>");
        resp.getWriter().println("<br><h1>Lien :" + req.getRequestURI() + "</h1>");

        for (Class<?> controllerClass : listeController) {
            AnnotationController annotation = controllerClass.getAnnotation(util.Annotation.AnnotationController.class);
            if (annotation != null) {
                String nameController = controllerClass.getSimpleName();
                resp.getWriter().println("<br>controller :" + nameController);
            } else {
                resp.getWriter().println("<br>Annotation nulle");
            }
        }
       executeUrl(req,resp,test);
    }
}
