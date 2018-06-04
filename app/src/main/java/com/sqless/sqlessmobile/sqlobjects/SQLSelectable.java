package com.sqless.sqlessmobile.sqlobjects;

/**
 * The interface that must be implemented by SQL Objects compatible with the
 * keyword 'SELECT'.
 *
 * @author David Orquin, Tomás Casir, Valeria Fornieles
 */
public interface SQLSelectable {

    /**
     * The constant to refer to all the rows in a table. Passing this to
     * {@link #getSelectStatement(int)} will not add the LIMIT keyword.
     */
    public static final int ALL = -1;

    /**
     * Creates an SQL {@code SELECT} statement compatible with the
     * {@code SQLObject} implementing this interface.
     *
     * @param limit the number of rows to display.
     * @return an SQL {@code SELECT} statement ready to be executed.
     */
    String getSelectStatement(int limit);
}

