package com.sqless.sqlessmobile.sqlobjects;

import java.util.ArrayList;
import java.util.List;

public class SQLTable extends SQLObject {

    private List<SQLColumn> columns;
    private List<SQLForeignKey> foreignKeys;

    public SQLTable(String nombre) {
        super(nombre);
        columns = new ArrayList<>();
        foreignKeys = new ArrayList<>();
    }

    public void addColumn(SQLColumn column) {
        columns.add(column);
    }

    public void addFK(SQLForeignKey fk) {
        foreignKeys.add(fk);
    }

    public List<SQLColumn> getColumns() {
        return columns;
    }

    @Override
    public String toString() {
        return "SQLTable{" +
                "name='" + getNombre() + '\'' +
                ", columns=" + columns +
                '}';
    }
}
