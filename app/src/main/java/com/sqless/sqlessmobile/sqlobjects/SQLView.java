package com.sqless.sqlessmobile.sqlobjects;

public class SQLView extends SQLObject implements SQLDroppable, SQLSelectable {

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

    @Override
    public String getSelectStatement(int limit) {
        return "SELECT * FROM `" + getName() + "`" + (limit == SQLSelectable.ALL ? "" : " LIMIT " + limit);
    }
}
