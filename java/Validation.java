package util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Validation {

    public static Map<String, String> validate(Object paramObject) {
        Map<String, String> errors = new HashMap<>();
        
        if (paramObject == null) {
            errors.put("object", "L'objet ne peut pas être null");
            return errors;
        }
        
        Class<?> clazz = paramObject.getClass();
        
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Object value;
            
            try {
                value = field.get(paramObject); // Récupération de la valeur
            } catch (IllegalAccessException e) {
                errors.put(field.getName(), "Impossible d'accéder au champ");
                continue;
            }
            
            // Ajout des validations
            if (field.isAnnotationPresent(Annotation.Required.class)) {
                validateRequired(value, field.getAnnotation(Annotation.Required.class), errors, field.getName());
            }

            if (field.isAnnotationPresent(Annotation.Number.class)) {
                validateNumber(value, field.getAnnotation(Annotation.Number.class), errors, field.getName());
            }

            if (field.isAnnotationPresent(Annotation.Email.class)) {
                validateEmail(value, field.getAnnotation(Annotation.Email.class), errors, field.getName());
            }

            if (field.isAnnotationPresent(Annotation.Size.class)) {
                validateSize(value, field.getAnnotation(Annotation.Size.class), errors, field.getName());
            }
        }
        
        return errors;
    }

    private static void validateRequired(Object value, Annotation.Required required, Map<String, String> errors, String fieldName) {
        if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
            String message = required.message().isEmpty() 
                ? "Le champ " + fieldName + " est obligatoire"
                : required.message();
            errors.put(fieldName, message);
        }
    }

    private static void validateNumber(Object value, Annotation.Number number, Map<String, String> errors, String fieldName) {
        if (value == null) return;

        String strValue = value.toString();
        try {
            Double.parseDouble(strValue);
        } catch (NumberFormatException e) {
            String message = number.message().isEmpty() 
                ? "Le champ " + fieldName + " doit être un nombre"
                : number.message();
            errors.put(fieldName, message);
        }
    }

    private static void validateEmail(Object value, Annotation.Email email, Map<String, String> errors, String fieldName) {
        if (value == null) return;

        String strValue = value.toString();
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!Pattern.compile(emailRegex).matcher(strValue).matches()) {
            String message = email.message().isEmpty() 
                ? "Le champ " + fieldName + " doit être une adresse email valide"
                : email.message();
            errors.put(fieldName, message);
        }
    }

    private static void validateSize(Object value, Annotation.Size size, Map<String, String> errors, String fieldName) {
        if (value == null) return;

        String strValue = value.toString();
        String sizeValue = size.value();
        if (sizeValue.contains("-")) {
            String[] limits = sizeValue.split("-");
            int min = Integer.parseInt(limits[0]);
            int max = Integer.parseInt(limits[1]);
            if (strValue.length() < min || strValue.length() > max) {
                String message = size.message().isEmpty() 
                    ? "Le champ " + fieldName + " doit avoir une taille entre " + min + " et " + max
                    : size.message();
                errors.put(fieldName, message);
            }
        } else {
            int exactSize = Integer.parseInt(sizeValue);
            if (strValue.length() != exactSize) {
                String message = size.message().isEmpty() 
                    ? "Le champ " + fieldName + " doit avoir une taille de " + exactSize
                    : size.message();
                errors.put(fieldName, message);
            }
        }
    }
}
