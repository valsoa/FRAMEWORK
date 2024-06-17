package util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.http.HttpResponse;
import java.rmi.server.ServerCloneException;
import java.util.ArrayList;
import java.util.Date;
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
<<<<<<< Updated upstream
<<<<<<< Updated upstream
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
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
        ServletContext context = getServletContext();
        listeController = Util.allMappingUrls(packages,util.Annotation.AnnotationController.class,context);
        urlMapping =Util.getUrlMapping(listeController);

    }     

    public Object[] getAllParams(Method method,HttpServletRequest req)throws IllegalArgumentException{
        Parameter[] parametres = method.getParameters();
        Object[] params = new Object[parametres.length];

        for (int i = 0; i < parametres.length; i++) {
            String nameParam ="";
            if(parametres[i].isAnnotationPresent(util.Annotation.AnnotationParameter.class)){
                nameParam=parametres[i].getAnnotation(util.Annotation.AnnotationParameter.class).value();
            }
            else{
                nameParam=parametres[i].getName();
            }
           String value = req.getParameter(nameParam) ;
            Class<?> typeParametre = parametres[i].getType();
            if (value == null) {
                throw new IllegalArgumentException("paramatre recquis :"+ nameParam);
            } 
            if (typeParametre== int.class) {
                params[i]= Integer.parseInt(value);
            }
            else if (typeParametre== Date.class || typeParametre == java.sql.Date.class) {
                try{
                    params[i] =java.sql.Date.valueOf(value);
                }
                catch(IllegalArgumentException e){
                    throw new IllegalArgumentException("le format de la date n'est pas valide:" + nameParam);
                }
            }
            else if(typeParametre == double.class){
                params[i]= Double.parseDouble(value);
            }
            else if (typeParametre == boolean.class) {
                params[i]= Boolean.parseBoolean(value);
            }
            else{
                params[i]=value;
            }
        }
        return params;
    }               

    public Object getValue(Mapping mapping, String className,HttpServletRequest req){
        try{
            Class<?> clas =Class.forName(className);
            Method method =clas.getMethod(mapping.getMethodName(),mapping.getTypes()); 
            Object obj = clas.newInstance();
            Object[] objectParam=getAllParams(method,req);
            return method.invoke(obj,objectParam);
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
                Object urlValue= getValue(value,value.getClassName(),req);
                resp.getWriter().println("<br>valeur:" + value.getClassName() +"_"+ value.getMethodName());
                if(urlValue instanceof String s){
                    resp.getWriter().println("<br>valeur methode:"+s);
                }
                else if(urlValue instanceof ModelView m){
                    sendModelView(m,req,resp);
                }
                else{
                    // resp.getWriter().println("<br>type de retour non valide"); tsy azo ato zaooo
                    throw new IllegalArgumentException("type de retour non valide");
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
<<<<<<< Updated upstream
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
                test=true;
                break;
            }    
        }
        if (!test) {
            resp.getWriter().println("<br>not found");
        }
=======
       executeUrl(req,resp,test);
>>>>>>> Stashed changes
    }
}
