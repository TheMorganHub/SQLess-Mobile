package com.sqless.sqlessmobile.sqlobjects;

public class SQLParameter extends SQLObject {
    
    private String dataType;
    private Object value;

    public SQLParameter(String name, String dataType) {
        super(name);
        this.dataType = dataType;
    }

    public String getDataType() {
        return dataType;
    }

    public void assignValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getName();
    }
    
}
