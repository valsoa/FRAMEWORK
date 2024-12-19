package util;

import java.io.File;
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

    protected Object typage(String paramValue, String paramName, Class<?> paramType)throws Exception {
        if (paramValue == null || paramType == null || paramValue.trim().isEmpty()) {
            System.out.println("Valeur ou type null pour le champ : " + paramName);
            return null;
        }
    
        if (paramType == Date.class || paramType == java.sql.Date.class) {
            return java.sql.Date.valueOf(paramValue); 
        }

        if (paramType == int.class || paramType == Integer.class) {
            return Integer.parseInt(paramValue);
        }

        if (paramType == double.class || paramType == Double.class) {
            return Double.parseDouble(paramValue);
        }

        if (paramType == float.class || paramType == Float.class) {
            return Float.parseFloat(paramValue);
        }

        if (paramType == long.class || paramType == Long.class) {
            return Long.parseLong(paramValue);
        }

        if (paramType == boolean.class || paramType == Boolean.class) {
            return Boolean.parseBoolean(paramValue); 
        }

        if (paramType == String.class) {
            return paramValue;
        }
             return null;
    }
    
    public Object[] getAllParams(Method method, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Parameter[] parametres = method.getParameters();
        Object[] params = new Object[parametres.length];
    
        for (int i = 0; i < parametres.length; i++) {
            String nameParam = parametres[i].isAnnotationPresent(util.Annotation.AnnotationParameter.class) 
                ? parametres[i].getAnnotation(util.Annotation.AnnotationParameter.class).value()
                : parametres[i].getName();
    
            Class<?> typeParametre = parametres[i].getType();
    
            if (typeParametre.equals(MySession.class)) {
                params[i] = new MySession(req.getSession());
                continue;
            }
    
            if (typeParametre.equals(FileUpload.class)) {
                params[i] = handleFileUpload(req, nameParam);
                continue;
            }
    
            if (isComplexObject(typeParametre)) {
                Object paramObject = createAndFillComplexObject(typeParametre, nameParam, req);
                validateObject(paramObject, nameParam, typeParametre, req, resp);
                params[i] = paramObject;
                continue;
            }
    
            String paramValue = req.getParameter(nameParam);
    
            validateObject(paramValue, nameParam, String.class, req, resp);
    
            params[i] = typage(paramValue, nameParam, typeParametre);
        }
    
        return params;
    }
    
    private boolean isComplexObject(Class<?> type) {
        return !type.isPrimitive() && !type.equals(String.class) && 
               !type.equals(MySession.class) && !type.equals(FileUpload.class);
    }
    
    private Object createAndFillComplexObject(Class<?> type, String nameParam, HttpServletRequest req) throws Exception {
        Object paramObject = type.getDeclaredConstructor().newInstance();
        Field[] fields = type.getDeclaredFields();

        for (Field field : fields) {
            String fieldName = field.getName();
            String fieldValue = req.getParameter(nameParam + "." + fieldName);
            System.out.println("Parametre reçu pour " + fieldName + ": " + fieldValue);

            if (fieldValue != null && !fieldValue.trim().isEmpty()) {
                field.setAccessible(true);
                Object typedValue = typage(fieldValue, fieldName, field.getType());
                System.out.println("Valeur typee pour " + fieldName + ": " + typedValue);
                field.set(paramObject, typedValue);
            } else {
                System.out.println("Parametre manquant ou vide pour " + fieldName);
            }
        }

        return paramObject;
    }
    
    private void validateObject(Object paramValue, String nameParam, Class<?> typeParametre, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Map des erreurs de validation
        Map<String, String> errorMap = new HashMap<>();
        // Map des valeurs du formulaire
        Map<String, String> formData = new HashMap<>();
        
        // Vérification de la nullité de l'objet
        if (paramValue == null) {
            errorMap.put(nameParam, "Le paramètre " + nameParam + " ne peut pas être null");
        } else {
            // Appel de la méthode de validation
            Map<String, String> validationErrors = Validation.validate(paramValue);
            
            // Ajouter les erreurs de validation à errorMap
            if (!validationErrors.isEmpty()) {
                errorMap.putAll(validationErrors);
            }
        }
    
        // Ajouter les données saisies au formulaire
        req.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                formData.put(key, values[0]);
            }
        });
    
        if (!errorMap.isEmpty()) {
           // Sinon, stocker les erreurs et les données dans la session et rediriger vers la page précédente
           req.getSession().setAttribute("validationErrors", errorMap);
           req.getSession().setAttribute("formData", formData);
   
           // Redirection vers la page précédente
           String referer = req.getHeader("Referer");
           if (referer != null && !referer.isEmpty()) {
               resp.sendRedirect(referer);
           } 
        }
    }
    
    

    public Object getValue(Mapping mapping, String methodeName, String className, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Class<?> clas = Class.forName(className);
        Object obj = clas.newInstance();
        Method[] methods = clas.getMethods();

        for (Method method2 : methods) {
            if (method2.getName().equalsIgnoreCase(methodeName)) {
                method2.setAccessible(true);
                // Passer 'resp' correctement à getAllParams
                Object[] objectParam = getAllParams(method2, req, resp); // Passer 'resp'

                // Vérification pour @RestApi et gestion de la réponse JSON
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

        // Assurez-vous que resp est passé ici dans l'appel de getAllParams
        Object urlValue = getValue(mapping, methodeName, mapping.getClassName(), req, resp);

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
