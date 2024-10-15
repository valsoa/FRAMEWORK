package util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.List;
import java.sql.Date;
import util.Annotation.*;
import com.google.gson.Gson;

public class FrontController extends HttpServlet {
    private List<Class<?>> listeController;
    private HashMap<String, Mapping> urlMapping = new HashMap<>();

    public void init(ServletConfig conf) throws ServletException {
        super.init(conf);
        String packages = this.getInitParameter("package");
        ServletContext context = getServletContext();
        listeController = Util.allMappingUrls(packages, util.Annotation.AnnotationController.class, context);
        urlMapping = Util.getUrlMapping(listeController);
        System.out.println("URL Mappings: " + urlMapping); 
    }

    protected Object typage(String paramValue, String paramName, Class<?> paramType) {
        Object o = null;
        if (paramType == Date.class || paramType == java.sql.Date.class) {
            try {
                o = java.sql.Date.valueOf(paramValue);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid date format for parameter: " + paramName);
            }
        } else if (paramType == int.class) {
            o = Integer.parseInt(paramValue);
        } else if (paramType == double.class) {
            o = Double.parseDouble(paramValue);
        } else if (paramType == boolean.class) {
            o = Boolean.parseBoolean(paramValue);
        } else {
            o = paramValue;
        }
        return o;
    }

    public Object[] getAllParams(Method method, HttpServletRequest req) {
        Parameter[] parametres = method.getParameters();
        Object[] params = new Object[parametres.length];

        for (int i = 0; i < parametres.length; i++) {
            String nameParam = "";
            if (parametres[i].isAnnotationPresent(util.Annotation.AnnotationParameter.class)) {
                nameParam = parametres[i].getAnnotation(util.Annotation.AnnotationParameter.class).value();
            } else {
                nameParam = parametres[i].getName();
                throw new IllegalArgumentException("ETU002420: Parameter " + nameParam + " is missing an annotation.");
            }
            Class<?> typeParametre = parametres[i].getType();
            if (!typeParametre.isPrimitive() && !typeParametre.equals(String.class) && !typeParametre.equals(MySession.class)) {
                try {
                    Object paramObject = typeParametre.getDeclaredConstructor().newInstance();
                    Field[] fields = typeParametre.getDeclaredFields();

                    for (Field field : fields) {
                        String fieldName = field.getName();
                        String fieldValue = req.getParameter(nameParam + "." + fieldName);
                        if (fieldValue != null) {
                            field.setAccessible(true);
                            Object typedValue = typage(fieldValue, fieldName, field.getType());
                            field.set(paramObject, typedValue);
                        }
                    }
                    params[i] = paramObject;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new IllegalArgumentException("Error creating parameter object: " + nameParam, e);
                }
            } else if (typeParametre.equals(MySession.class)) {
                params[i] = new MySession(req.getSession());
            } else {
                String paramValue = req.getParameter(nameParam);
                if (paramValue == null) {
                    throw new IllegalArgumentException("Missing parameter: " + nameParam);
                }
                params[i] = typage(paramValue, nameParam, typeParametre);
            }
        }
        return params;
    }

    public Object getValue(Mapping mapping, String methodeName, String className, HttpServletRequest req) throws Exception {
        Class<?> clas = Class.forName(className);
        Object obj = clas.newInstance();
        Method[] methods = clas.getMethods();

        for (Method method2 : methods) {
            if (method2.getName().equalsIgnoreCase(methodeName)) {
                method2.setAccessible(true);
                Object[] objectParam = getAllParams(method2, req);
                
                // Check for @RestApi and handle JSON response
                if (method2.isAnnotationPresent(util.Annotation.RestApi.class)) {
                    Object result = method2.invoke(obj, objectParam);
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(result);
                    req.setAttribute("jsonResponse", jsonResponse);
                    return jsonResponse;
                }
                
                return method2.invoke(obj, objectParam);
            }
        }
        return null;
    }

    public void sendModelView(ModelView model, HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException {
        for (Map.Entry<String, Object> entry : model.getData().entrySet()) {
            req.setAttribute(entry.getKey(), entry.getValue());
        }
        RequestDispatcher dispatch = req.getRequestDispatcher(model.getUrl());
        dispatch.forward(req, rep);
    }

    public void executeUrl(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, Exception {
        String requestURI = req.getRequestURI();
        String httpMethod = req.getMethod(); // GET ou POST
        System.out.println(httpMethod + ":" + requestURI);
        String methodeName = "";
        Mapping mapping = urlMapping.get(requestURI);
    

        if (mapping == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "404 Not Found: The requested URL was not found on this server.");
            return; // On arrête l'exécution
        }
    
        Class<?> clas = Class.forName(mapping.getClassName());
        boolean verbFound = false;
    
        for (VerbAction verbAction : mapping.getVerbActions()) {
            if (verbAction.getHttpMethod().equalsIgnoreCase(req.getMethod())) {
                verbFound = true;
                methodeName = verbAction.getMethodName();
                break;
            }
        }
    
        if (!verbFound) {
            throw new Exception("HTTP 405 Method Not Allowed");
        }
    
        Object urlValue = getValue(mapping, methodeName, mapping.getClassName(), req);
        if (urlValue instanceof String jsonResponse) {
            resp.setContentType("application/json");
            resp.getWriter().write(jsonResponse);
        } else if (urlValue instanceof ModelView model) {
            sendModelView(model, req, resp);
        } else {
            throw new IllegalArgumentException("Invalid return type: " + urlValue.getClass());
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            resp.setContentType("text/html");
            executeUrl(req, resp);
        } catch (Exception e) {
            e.printStackTrace(resp.getWriter());
        }
    }
}
