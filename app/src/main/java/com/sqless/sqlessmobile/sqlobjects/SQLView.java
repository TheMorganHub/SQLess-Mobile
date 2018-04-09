package com.sqless.sqlessmobile.sqlobjects;

public class SQLView extends SQLObject implements SQLDroppable {

    public SQLView(String name) {
        super(name);
    }

    @Override
    public String getDropStatement() {
        return "DROP VIEW `" + getName() + "`";
    }

    @Override
    public String toString() {
        return getName();
    }
}
