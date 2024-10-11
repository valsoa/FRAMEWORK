package util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Verb {
    @Retention(RetentionPolicy.RUNTIME) 
    @Target(ElementType.METHOD)
    public @interface GET {
    }

    @Retention(RetentionPolicy.RUNTIME) 
    @Target(ElementType.METHOD)
    public @interface POST {
    }
}
