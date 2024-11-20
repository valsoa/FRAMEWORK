package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import java.util.List;
import java.sql.Date;
import util.Annotation.*;
import com.google.gson.Gson;
import util.*;


@MultipartConfig
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
    
        // Vérification des types Date
        if (paramType == Date.class || paramType == java.sql.Date.class) {
            try {
                o = java.sql.Date.valueOf(paramValue);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid date format for parameter: " + paramName);
            }
        } 
        // Vérification pour les types numériques
        else if (paramType == int.class || paramType == Integer.class) {
            if (paramValue == null || paramValue.trim().isEmpty()) {
                // Si la valeur est vide ou null, retournez null ou lancez une exception de validation
                throw new IllegalArgumentException("Le champ " + paramName + " ne peut pas être vide et doit être un nombre.");
            }
            try {
                o = Integer.parseInt(paramValue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format for parameter: " + paramName);
            }
        } 
        else if (paramType == double.class || paramType == Double.class) {
            if (paramValue == null || paramValue.trim().isEmpty()) {
                // Si la valeur est vide ou null, retournez null ou lancez une exception de validation
                throw new IllegalArgumentException("Le champ " + paramName + " ne peut pas être vide et doit être un nombre.");
            }
            try {
                o = Double.parseDouble(paramValue);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number format for parameter: " + paramName);
            }
        } 
        // Vérification pour les types booléens
        else if (paramType == boolean.class || paramType == Boolean.class) {
            o = Boolean.parseBoolean(paramValue);
        }
        // Autres types (par exemple String)
        else {
            o = paramValue;
        }
    
        return o;
    }
    
    public Object[] getAllParams(Method method, HttpServletRequest req) throws IOException, ServletException, ValidationException {
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
            if (!typeParametre.isPrimitive() && !typeParametre.equals(String.class) && 
                !typeParametre.equals(MySession.class) && !typeParametre.equals(FileUpload.class)) {
                try {
                    Object paramObject = typeParametre.getDeclaredConstructor().newInstance();
                    Field[] fields = typeParametre.getDeclaredFields();
    
                    for (Field field : fields) {
                        String fieldName = field.getName();
                        String fieldValue = req.getParameter(nameParam + "." + fieldName);
                        if (fieldValue != null) {
                            field.setAccessible(true);
    
                            // Validation préalable avant de passer à la conversion du type
                            if (field.getType() == int.class && fieldValue.trim().isEmpty()) {
                                throw new ValidationException("Le champ " + fieldName + " ne peut pas être vide et doit être un nombre.");
                            }
    
                            Object typedValue = typage(fieldValue, fieldName, field.getType());
                            field.set(paramObject, typedValue);
                        }
                    }
    
                    // Validation après avoir rempli l'objet paramObject
                    List<String> validationErrors = Validation.validate(paramObject);
                    if (!validationErrors.isEmpty()) {
                        req.setAttribute("validationErrors", validationErrors);
                        // Lancer une ValidationException directement si des erreurs de validation sont présentes
                        throw new ValidationException("Validation failed for parameter: " + nameParam + ". Errors: " + String.join(", ", validationErrors));
                    }
    
                    params[i] = paramObject;
                } catch (ValidationException e) {
                    // Gérer l'exception de validation ici, ou la relancer selon le cas
                    throw e;  // Propager l'exception de validation pour qu'elle soit traitée ailleurs
                } catch (Exception e) {
                    // Si d'autres erreurs se produisent (création d'objet, etc.)
                    throw new IllegalArgumentException("Error creating parameter object: " + nameParam, e);
                }
            } else if (typeParametre.equals(MySession.class)) {
                params[i] = new MySession(req.getSession());
            } else if (typeParametre.equals(FileUpload.class)) {
                params[i] = handleFileUpload(req, nameParam);
            } else {
                String paramValue = req.getParameter(nameParam);
                if (paramValue == null || paramValue.trim().isEmpty()) {
                    if (typeParametre.isPrimitive()) {
                        // Valeurs par défaut pour les types primitifs
                        if (typeParametre == int.class) {
                            paramValue = "0";
                        } else if (typeParametre == double.class) {
                            paramValue = "0.0";
                        } else if (typeParametre == boolean.class) {
                            paramValue = "false";
                        }
                    } else {
                        throw new IllegalArgumentException("Missing parameter: " + nameParam);
                    }
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
                
                // verifie  for @RestApi et  handle JSON response
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


    public static FileUpload handleFileUpload(HttpServletRequest request, String inputFileParam) throws IOException, ServletException {
        Part filePart = request.getPart(inputFileParam); 
        String fileName = extractFileName(filePart);
        byte[] fileContent = filePart.getInputStream().readAllBytes();

        String uploadDir = request.getServletContext().getRealPath("") + "uploads\\" + fileName;
        System.out.println("upload = "+uploadDir);

        File uploadFolder = new File(uploadDir);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs(); 
        }

        System.out.println("upload = "+uploadDir);

        filePart.write(uploadDir);

        return new FileUpload(fileName, "uploads\\" + fileName, fileContent);
    }

    private static String extractFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        String[] items = contentDisposition.split(";");
        for (String item : items) {
            if (item.trim().startsWith("filename")) {
                return item.substring(item.indexOf("=") + 2, item.length() - 1);
            }
        }
        return "";
    }


    public void executeUrl(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, Exception {
    PrintWriter out = resp.getWriter();
    String requestURI = req.getRequestURI();
    String httpMethod = req.getMethod();
    String methodeName = "";
    Mapping mapping = urlMapping.get(requestURI);

    if (mapping == null) {
        out.println("404 Not Found: The requested URL was not found on this server.");
        return;
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
    } 
    else {
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
