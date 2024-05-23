package util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface Annotation {
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnotationController{
    }
    @Retention(RetentionPolicy.RUNTIME) 
    public @interface Get{
        String value();
    }
}
