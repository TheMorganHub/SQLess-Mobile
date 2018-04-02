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

    public String generateCreatePKsStatement() {
        StringBuilder pkSb = new StringBuilder("PRIMARY KEY (");
        boolean atLeastOnePk = false;
        for (int i = 0; i < columns.size(); i++) {
            SQLColumn col = columns.get(i);
            if (col.isPK()) {
                pkSb.append(atLeastOnePk ? "," : "").append("`").append(col.getName()).append("`");
                atLeastOnePk = true;
            }
        }
        return atLeastOnePk ? pkSb.append(")").toString() : "";
    }

    public String generateCreateStatement() {
        if (columns.isEmpty()) {
            return "";
        }

        StringBuilder createTableSb = new StringBuilder("CREATE TABLE ").append("`").append(getName()).append("` (\n");
        boolean hasPK = false;
        boolean hasFK = !foreignKeys.isEmpty();

        for (int i = 0; i < columns.size(); i++) {
            SQLColumn col = columns.get(i);
            if (col.isPK()) {
                hasPK = true;
            }
            createTableSb.append(col.getCreateLine()).append(i < columns.size() - 1 || hasPK || hasFK ? "," : "").append("\n");
        }
        createTableSb.append(hasPK ? generateCreatePKsStatement() : "").append(hasPK && hasFK ? "," : "").append("\n");

        for (int i = 0; i < foreignKeys.size(); i++) {
            SQLForeignKey fk = foreignKeys.get(i);
            createTableSb.append(fk.getStatementForCreate()).append(i < foreignKeys.size() - 1 ? "," : "").append("\n");
        }

        return createTableSb.append(");").toString();
    }

    @Override
    public String toString() {
        return "SQLTable{" +
                "name='" + getName() + '\'' +
                ", columns=" + columns +
                '}';
    }
}
