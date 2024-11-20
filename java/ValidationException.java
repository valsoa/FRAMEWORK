package util;
import java.util.*;

public class ValidationException extends Exception {
    private List<String> validationErrors;

    public ValidationException(String message) {
        super(message);
        this.validationErrors = new ArrayList<>();
    }

    public ValidationException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public ValidationException(String message, List<String> validationErrors, Throwable cause) {
        super(message, cause);
        this.validationErrors = validationErrors;
    }

    /**
     * Récupère la liste des erreurs de validation
     * @return List<String> contenant les messages d'erreur
     */
    public List<String> getValidationErrors() {
        return validationErrors;
    }

    /**
     * Ajoute une erreur de validation à la liste
     * @param error Le message d'erreur à ajouter
     */
    public void addValidationError(String error) {
        if (this.validationErrors == null) {
            this.validationErrors = new ArrayList<>();
        }
        this.validationErrors.add(error);
    }

    /**
     * Définit la liste des erreurs de validation
     * @param validationErrors La nouvelle liste d'erreurs
     */
    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    /**
     * Vérifie si des erreurs de validation sont présentes
     * @return true s'il y a des erreurs, false sinon
     */
    public boolean hasErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }

    @Override
    public String getMessage() {
        if (validationErrors == null || validationErrors.isEmpty()) {
            return super.getMessage();
        }
        return super.getMessage() + ": " + String.join(", ", validationErrors);
    }
}