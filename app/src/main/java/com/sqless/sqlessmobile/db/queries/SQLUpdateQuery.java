package com.sqless.sqlessmobile.db.queries;

import com.sqless.sqlessmobile.network.SQLConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

public class SQLUpdateQuery extends SQLQuery {

    /**
     * Crea una nueva {@code UPDATE} query. La query se ejecutará en un thread diferente al
     * que creó este objeto.
     *
     * @param connectionData
     * @param sql
     */
    public SQLUpdateQuery(SQLConnectionManager.ConnectionData connectionData, String sql) {
        super(connectionData, sql, true);
    }

    /**
     * Crea una nueva {@code UPDATE} query. El usuario tiene la opción de elegir si la query se
     * ejecutará en el mismo thread que construyó este objeto u otro nuevo.
     *
     * @param connectionData
     * @param sql
     * @param newThread
     */
    public SQLUpdateQuery(SQLConnectionManager.ConnectionData connectionData, String sql, boolean newThread) {
        super(connectionData, sql, newThread);
    }

    @Override
    protected void doExecute() {
        try {
            Connection conFromData = SQLConnectionManager.getInstance().getConnection(connectionData);
            if (conFromData != null) {
                statement = conFromData.createStatement();
                int affectedRows = statement.executeUpdate(getSql());
                onSuccess(affectedRows);
                querySuccess = true;
            } else {
                throw new SQLException("La conexión es nula");
            }
        } catch (SQLException ex) {
            onFailure(ex.getMessage());
        } finally {
            closeQuery();
        }
    }

    /**
     * Called upon successful completion of a query. This method is empty by
     * default. Children that inherit from this class have the option to
     * override it.
     *
     * @param updateCount for Update queries, the number of rows that were
     *                    updated by this query.
     */
    public void onSuccess(int updateCount) {
    }
}
