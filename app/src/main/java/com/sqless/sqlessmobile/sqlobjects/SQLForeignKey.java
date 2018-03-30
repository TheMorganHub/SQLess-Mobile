package com.sqless.sqlessmobile.sqlobjects;

public class SQLForeignKey extends SQLObject {

    private String columnName;
    private String referencedTable;
    private String referencedColumn;

    public SQLForeignKey(String nombre, String columnName, String referencedTable, String referencedColumn) {
        super(nombre);
        this.columnName = columnName;
        this.referencedTable = referencedTable;
        this.referencedColumn = referencedColumn;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getReferencedTable() {
        return referencedTable;
    }

    public String getReferencedColumn() {
        return referencedColumn;
    }

    @Override
    public String toString() {
        return getNombre() + " (" + columnName + ") → " + referencedTable + " (" + referencedColumn + ")";
    }
}
