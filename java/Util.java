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

    public static HashMap<String, Mapping> getUrlMapping(List<Class<?>> listeController) {
        HashMap<String, Mapping> result = new HashMap<>();
        for (Class<?> class1 : listeController) {
            Method[] methodes = class1.getDeclaredMethods();
            for (Method method : methodes) {
                if (method.isAnnotationPresent(util.Annotation.Get.class)) {
                    if (result.containsKey(method.getAnnotation(util.Annotation.Get.class).value())) {
                        throw new IllegalArgumentException("L'URL " + method.getAnnotation(util.Annotation.Get.class).value() + " a été dupliquée");
                    }
                    result.put(method.getAnnotation(util.Annotation.Get.class).value(),
                            new Mapping(class1.getName(), method.getName(),method.getParameterTypes()));
                }
            }
        }
        return result;
    }

    public static void checkPackageExists(String packageName, ServletContext context) {
        String packagePath = "/WEB-INF/classes/" + packageName.replace('.', '/');
        Set<String> resourcePaths = context.getResourcePaths(packagePath);
        if (resourcePaths == null || resourcePaths.isEmpty()) {
            throw new IllegalArgumentException("Le package " + packageName + " n'existe pas ou n'est pas configuré dans init-param.");
        }
    }

    public static List<Class<?>> allMappingUrls(String packageNames, Class<? extends Annotation> annotationClass, ServletContext context) {
        listeController = new ArrayList<>();
        
        // Divisez la chaîne de packages en une liste de packages
        String[] packages = packageNames.split(",");
        
        for (String packageName : packages) {
            packageName = packageName.trim();
            // Vérifiez si le package existe dans le contexte
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
        }
        
        return listeController;
    }
}
