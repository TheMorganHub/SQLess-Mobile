package com.sqless.sqlessmobile.sqlobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SQLTable extends SQLObject implements SQLDroppable, SQLRenameable, SQLSelectable {

    private List<SQLColumn> columns;
    private List<SQLForeignKey> foreignKeys;

    public SQLTable(String nombre) {
        super(nombre);
        columns = new ArrayList<>();
        foreignKeys = new ArrayList<>();
    }

    public void setColumns(List<SQLColumn> columns) {
        this.columns = columns;
    }

    public void addColumn(SQLColumn column) {
        columns.add(column);
    }

    public void removeColumn(SQLColumn column) {
        columns.remove(column);
    }

    public void addFK(SQLForeignKey fk) {
        foreignKeys.add(fk);
    }

    public void removeFK(SQLForeignKey fk) {
        foreignKeys.remove(fk);
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
    public String getDropStatement() {
        return "DROP TABLE `" + getName() + "`";
    }

    @Override
    public String getRenameStatement(String newName) {
        return "RENAME TABLE `" + getName() + "` to `" + newName + "`";
    }

    public String getInsertIntoStatement(Map<String, String> columnDataPairs) {
        StringBuilder stmtBuilder = new StringBuilder("INSERT INTO `" + getName() + "`");
        StringBuilder colBuilder = new StringBuilder("(");
        StringBuilder valueBuilder = new StringBuilder("VALUES(");
        boolean first = true;
        for (Map.Entry<String, String> dataPair : columnDataPairs.entrySet()) {
            String columnName = "`" + dataPair.getKey() + "`";
            String columnData = "'" + dataPair.getValue() + "'";
            if (first) {
                colBuilder.append(columnName);
            } else {
                colBuilder.append(",").append(columnName);
            }
            if (first) {
                valueBuilder.append(columnData);
            } else {
                valueBuilder.append(",").append(columnData);
            }
            first = false;
        }
        valueBuilder.append(")");
        colBuilder.append(") ");
        stmtBuilder.append(colBuilder).append(valueBuilder);
        return stmtBuilder.toString();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getSelectStatement(int limit) {
        return "SELECT * FROM `" + getName() + "`" + (limit == SQLSelectable.ALL ? "" : " LIMIT " + limit);
    }
}
