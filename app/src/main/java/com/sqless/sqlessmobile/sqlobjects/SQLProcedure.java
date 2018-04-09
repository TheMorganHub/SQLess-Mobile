package com.sqless.sqlessmobile.sqlobjects;

public class SQLProcedure extends SQLExecutable {

    public SQLProcedure(String name) {
        super(name);
    }

    @Override
    public String getCallStatement() {
        return "CALL " + getName() + "(" + prepareParameters() + ")";
    }

    @Override
    public String getDropStatement() {
        return "DROP PROCEDURE IF EXISTS `" + getName() + "`";
    }
}
