package util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface Annotation {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnotationController{
    }
    @Retention(RetentionPolicy.RUNTIME) 
    public @interface Get{
        String value();
    }
    @Retention(RetentionPolicy.RUNTIME) 
    public @interface AnnotationParameter{
        String value();
    }
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RestApi {
        String value() default ""; 
    }
}
