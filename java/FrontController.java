package util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.rmi.server.ServerCloneException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Annotation.*;
import java.lang.annotation.Annotation;


public class FrontController extends HttpServlet {
    private List<Class<?>> listeController;
    private HashMap<String,Mapping> urlMapping = new HashMap<>();

    public void init(ServletConfig conf) throws ServletException {
        super.init(conf);
        String packages = this.getInitParameter("package");
        listeController = allMappingUrls(packages,util.Annotation.AnnotationController.class);
        urlMapping = getUrlMapping(listeController);
    }

    public HashMap<String,Mapping> getUrlMapping(List<Class<?>> listeController){
        HashMap<String,Mapping> result = new HashMap<>();
        for (Class<?> class1 : listeController) {
            Method[] methodes = class1.getDeclaredMethods();
            for (Method method : methodes) {
                if(method.isAnnotationPresent(util.Annotation.Get.class)){
                    result.put(method.getAnnotation(util.Annotation.Get.class).value(),
                    new Mapping(class1.getName(),method.getName()));
                }
            }
        }
        return result;
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
    public List<Class<?>> allMappingUrls(String packageName, Class<? extends Annotation> annotationClass) {
        listeController = new ArrayList<>();
        ServletContext context = getServletContext();
        String path = "/WEB-INF/classes/" + packageName.replace('.', '/');
    
        Set<String> classNames = context.getResourcePaths(path);
        if (classNames != null) {
            for (String className : classNames) {
                if (className.endsWith(".class")) {
                    String fullClassName = packageName + "." + className.substring(path.length() + 1, className.length() - 6).replace('/', '.');
                    
                    try {
                        Class<?> clazz = Class.forName(fullClassName);
                        Annotation annotation = clazz.getAnnotation(annotationClass);
                        if (annotation instanceof AnnotationController) {
                            AnnotationController controllerAnnotation = (AnnotationController) annotation;
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
        resp.getWriter().println("<br>urlMapping:"+urlMapping);
        for (Map.Entry<String,Mapping> entry : urlMapping.entrySet()) {
            String url = entry.getKey();
            Mapping value = entry.getValue();
            if(url.equals(req.getRequestURI())){
                resp.getWriter().println("<br>valeur:" + value.getClassName() +"_"+ value.getMethodName());
                resp.getWriter().println("<br>valeur methode:"+(String)getValue(value.getMethodName(),value.getClassName()));
                test=true;
                break;
            }    
        }
        if (!test) {
            resp.getWriter().println("<br>not found");
        }
    }
}
