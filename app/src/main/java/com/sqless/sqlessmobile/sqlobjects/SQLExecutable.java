package com.sqless.sqlessmobile.sqlobjects;

import java.util.ArrayList;
import java.util.List;

public abstract class SQLExecutable extends SQLObject implements SQLCallable, SQLDroppable {
    
    protected List<SQLParameter> parameters;

    public SQLExecutable(String name) {
        super(name);
    }
    
    public void addParameter(SQLParameter parameter) {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        parameters.add(parameter);
    }

    public List<SQLParameter> getParameters() {
        return parameters;
    }

    @Override
    public String prepareParameters() {
        StringBuilder paramsStmt = new StringBuilder();
        if (parameters != null) {
            for (SQLParameter parameter : parameters) {
                paramsStmt.append(parameter.getValue()).append(", ");
            }
        }
        return (parameters != null && !parameters.isEmpty() ? paramsStmt.substring(0, paramsStmt.length() - 2) : paramsStmt.toString());
    }
    
    @Override
    public String toString() {
        StringBuilder paramatersToString = new StringBuilder("(");
        if (parameters != null && !parameters.isEmpty()) {
            for (SQLParameter parameter : parameters) {
                paramatersToString.append(parameter).append(", ");
            }
        }
        return getName() + (parameters != null && !parameters.isEmpty() ? paramatersToString.substring(0, paramatersToString.length() - 2) + ")" : "()");
    }
    
}
