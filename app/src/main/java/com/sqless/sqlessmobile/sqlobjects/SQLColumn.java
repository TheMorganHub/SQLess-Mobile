package com.sqless.sqlessmobile.sqlobjects;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.Iconable;

import java.io.Serializable;

public class SQLColumn implements Iconable, Serializable {

    private String nombre;
    private String datatype;
    private boolean isPK;
    private boolean isFK;
    private boolean nullable;

    public SQLColumn(String nombre, String datatype) {
        this.nombre = nombre;
        this.datatype = datatype;
    }

    public SQLColumn(String nombre, String datatype, boolean isPK, boolean nullable) {
        this.nombre = nombre;
        this.datatype = datatype;
        this.isPK = isPK;
        this.nullable = nullable;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setIsPK(boolean flag) {
        this.isPK = flag;
    }

    public void setIsFK(boolean flag) {
        this.isFK = flag;
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

    @Override
    public String toString() {
        return nombre + " (" + datatype + ")";
    }
}