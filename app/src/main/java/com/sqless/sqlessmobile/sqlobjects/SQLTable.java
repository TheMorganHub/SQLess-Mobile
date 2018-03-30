package com.sqless.sqlessmobile.sqlobjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SQLTable implements Serializable {

    private String name;
    private List<SQLColumn> columns;

    public SQLTable() {
        columns = new ArrayList<>();
    }

    public void addColumn(SQLColumn column) {
        columns.add(column);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "SQLTable{" +
                "name='" + name + '\'' +
                ", columns=" + columns +
                '}';
    }
}
