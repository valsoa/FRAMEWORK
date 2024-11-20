package util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import util.Annotation;
import java.lang.reflect.Field;

public class Validation {
    public static List<String> validate(Object object) {
        List<String> errors = new ArrayList<>();
    
        // Récupérer tous les champs de l'objet
        Field[] fields = object.getClass().getDeclaredFields();
    
        for (Field field : fields) {
            field.setAccessible(true);
    
            try {
                Object value = field.get(object);
    
                // Vérification de @Required
                if (field.isAnnotationPresent(Annotation.Required.class)) {
                    Annotation.Required required = field.getAnnotation(Annotation.Required.class);
                    if (value == null || value.toString().trim().isEmpty()) {
                        String message = required.message().isEmpty() ? 
                            "Le champ " + field.getName() + " est obligatoire" : required.message();
                        errors.add(message);
                    }
                }
    
                if (value == null) continue; // Si la valeur est null, pas besoin de faire les autres vérifications
    
                // Vérification de @Number
                if (field.isAnnotationPresent(Annotation.Number.class)) {
                    Annotation.Number number = field.getAnnotation(Annotation.Number.class);
                    try {
                        Double.parseDouble(value.toString());
                    } catch (NumberFormatException e) {
                        String message = number.message().isEmpty() ? 
                            "Le champ " + field.getName() + " doit être un nombre" : number.message();
                        errors.add(message);
                    }
                }
    
                // Vérification de @Email
                if (field.isAnnotationPresent(Annotation.Email.class)) {
                    Annotation.Email email = field.getAnnotation(Annotation.Email.class);
                    String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
                    Pattern pattern = Pattern.compile(emailRegex);
                    if (!pattern.matcher(value.toString()).matches()) {
                        String message = email.message().isEmpty() ? 
                            "Le champ " + field.getName() + " doit être une adresse email valide" : email.message();
                        errors.add(message);
                    }
                }
    
                // Vérification de @Size
                if (field.isAnnotationPresent(Annotation.Size.class)) {
                    Annotation.Size size = field.getAnnotation(Annotation.Size.class);
                    String sizeValue = size.value();
                    if (!sizeValue.isEmpty()) {
                        String stringValue = value.toString();
                        if (sizeValue.contains("-")) {
                            String[] limits = sizeValue.split("-");
                            int min = Integer.parseInt(limits[0]);
                            int max = Integer.parseInt(limits[1]);
                            if (stringValue.length() < min || stringValue.length() > max) {
                                String message = size.message().isEmpty() ? 
                                    "Le champ " + field.getName() + " doit avoir une taille entre " + min + " et " + max : 
                                    size.message();
                                errors.add(message);
                            }
                        } else {
                            int exactSize = Integer.parseInt(sizeValue);
                            if (stringValue.length() != exactSize) {
                                String message = size.message().isEmpty() ? 
                                    "Le champ " + field.getName() + " doit avoir une taille de " + exactSize : 
                                    size.message();
                                errors.add(message);
                            }
                        }
                    }
                }
    
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    
        return errors;
    }
    
}
