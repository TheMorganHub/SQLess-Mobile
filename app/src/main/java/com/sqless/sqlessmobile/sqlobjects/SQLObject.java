package com.sqless.sqlessmobile.sqlobjects;

import java.io.Serializable;

public abstract class SQLObject implements Serializable {

    private String name;

    public SQLObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
