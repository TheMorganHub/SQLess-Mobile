package com.sqless.sqlessmobile.sqlobjects;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.Iconable;

public class SQLColumn extends SQLObject implements Iconable, SQLDroppable {

    private String parentName;
    private String datatype;
    private boolean isPK;
    private boolean isFK;
    private boolean nullable;

    public SQLColumn(String parentName, String nombre, String datatype) {
        super(nombre);
        this.parentName = parentName;
        this.datatype = datatype;
    }

    public SQLColumn(String parentName, String nombre, String datatype, boolean isPK, boolean nullable) {
        this(parentName, nombre, datatype);
        this.isPK = isPK;
        this.nullable = nullable;
    }

    public String getDatatype() {
        return datatype;
    }

    public String getDatatypeWithDefaultLength() {
        switch (datatype) {
            case "varchar":
                return "varchar(255)";
            case "decimal":
                return "decimal(10,2)";
        }
        return getDatatype();
    }

    public void setIsPK(boolean flag) {
        this.isPK = flag;
    }

    public void setIsFK(boolean flag) {
        this.isFK = flag;
    }

    public boolean isPK() {
        return isPK;
    }

    @Override
    public int getDrawableRes() {
        return isPK ? R.drawable.ic_primary_key_24dp : isFK ? R.drawable.ic_foreign_key_24dp : R.drawable.ic_column_24dp;
    }

    public void setNullable(boolean flag) {
        this.nullable = flag;
    }

    public boolean isNullable() {
        return nullable;
    }

    public String getCreateLine() {
        return "`" + getName() + "` " + getDatatypeWithDefaultLength() + " " + (isNullable() ? "NULL" : "NOT NULL");
    }

    @Override
    public String toString() {
        return getName() + " (" + datatype + ")";
    }

    @Override
    public String getDropStatement() {
        return "ALTER TABLE " + parentName + " DROP COLUMN `" + getName() + "`";
    }
}
