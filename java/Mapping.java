package  util;

public class Mapping {
    String className;
    String methodName;
    Class[] types;
    String verbe;
    
    public Class[] getTypes() {
        return types;
    }
    public void setTypes(Class[] types) {
        this.types = types;
    }
    public String getMethodName() {
        return methodName;
    }
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    
    public Mapping(String className, String methodName,Class[] types) {
        this.className = className;
        this.methodName = methodName;
        this.types = types;
    }
    public Mapping(String className, String methodName,Class[] types,String verb) {
        this.className = className;
        this.methodName = methodName;
        this.types = types;
        this.verbe=verb;
    }
    public String getVerbe() {
        return verbe;
    }
    public void setVerbe(String verbe) {
        this.verbe = verbe;
    }
}
