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
         public @interface URL{
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

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Required {
        String message() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Number {
        String message() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Email {
        String message() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Size {
        String value() default "";
        String message() default "";
    }
}
