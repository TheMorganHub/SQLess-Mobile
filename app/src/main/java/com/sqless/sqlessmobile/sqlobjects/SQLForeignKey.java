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

    public String getStatementForCreate() {
        return "CONSTRAINT `" + getName() + "` FOREIGN KEY (`" + getColumnName() + "`) REFERENCES `" + getReferencedTable()
                + "` (`" + getReferencedColumn() + "`) ON DELETE CASCADE ON UPDATE CASCADE";
    }

    @Override
    public String toString() {
        return getName() + " (" + columnName + ") → " + referencedTable + " (" + referencedColumn + ")";
    }
}
