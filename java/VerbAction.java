package util;

public class VerbAction {
    private String methodName;
    private String httpMethod;

    public VerbAction(String httpMethod, String methodName) {
        this.httpMethod = httpMethod;
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public VerbAction() {
    }
    
}
