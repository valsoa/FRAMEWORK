package util;

import java.util.ArrayList;
import java.util.List;

public class Mapping {
    private String className;
    private List<VerbAction> verbActions;
    private Class[] types;


    public Mapping(String className, Class[] types) {
        this.className = className;
        this.types = types;
        this.verbActions = new ArrayList<>(); 
    }

    public Class[] getTypes() {
        return types;
    }

    public void setTypes(Class[] types) {
        this.types = types;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<VerbAction> getVerbActions() {
        return verbActions;
    }

    public void setVerbActions(List<VerbAction> verbActions) {
        this.verbActions = verbActions;
    }

    public Mapping(String className, List<VerbAction> verbActions) {
        this.className = className;
        this.verbActions = verbActions;
    }
    
}
