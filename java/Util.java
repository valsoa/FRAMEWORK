package util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import jakarta.servlet.*;
import util.Annotation.AnnotationController;
import util.Mapping;

public class Util {
    public static List<Class<?>> listeController;

    // Récupérer le mappage des URL
    public static HashMap<String, Mapping> getUrlMapping(List<Class<?>> listeController) {
        HashMap<String, Mapping> result = new HashMap<>();
        for (Class<?> class1 : listeController) {
            Method[] methodes = class1.getDeclaredMethods();
            for (Method method : methodes) {
                if (method.isAnnotationPresent(util.Annotation.URL.class)){
                   String url = method.getAnnotation(util.Annotation.URL.class).value();
                   VerbAction verbAction= new VerbAction();
                   verbAction.setMethodName(method.getName());
                   verbAction.setHttpMethod(method.isAnnotationPresent(util.Verb.POST.class) ? "POST" : "GET");

                   if (!result.containsKey(url)) {
                        Mapping mapping= new Mapping(class1.getName(),new ArrayList<>());
                        mapping.getVerbActions().add(verbAction);
                        result.put(url, mapping); 
                   }
                   else{
                        Mapping exisMapping = result.get(url);
                        exisMapping.getVerbActions().add(verbAction);
                   }
                }
            }

        }
        return result;         
    }

    // Vérifier si le package existe
    public static void checkPackageExists(String packageName, ServletContext context) {
        String packagePath = "/WEB-INF/classes/" + packageName.replace('.', '/');
        Set<String> resourcePaths = context.getResourcePaths(packagePath);
        if (resourcePaths == null || resourcePaths.isEmpty()) {
            throw new IllegalArgumentException("Le package " + packageName + " n'existe pas ou n'est pas configuré dans init-param.");
        }
    }

    // Récupérer toutes les classes avec mappage d'URL
    public static List<Class<?>> allMappingUrls(String packageNames, Class<? extends Annotation> annotationClass, ServletContext context) {
        listeController = new ArrayList<>();

        String[] packages = packageNames.split(",");

        for (String packageName : packages) {
            packageName = packageName.trim();

            checkPackageExists(packageName, context);

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
                                listeController.add(clazz);
                            }
                        } catch (Exception e) {
                            System.out.println("Erreur lors de la récupération de la classe : " + e);
                        }
                    }
                }
            } else {
                System.out.println("Aucune classe trouvée dans le package : " + packageName);
            }
        }
        return listeController;
    }
}
