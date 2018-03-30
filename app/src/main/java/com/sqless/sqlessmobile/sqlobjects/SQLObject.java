package com.sqless.sqlessmobile.sqlobjects;

import java.io.Serializable;

public abstract class SQLObject implements Serializable {

    private String nombre;

    public SQLObject(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
