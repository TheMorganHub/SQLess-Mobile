package com.sqless.sqlessmobile.db.queries;

import com.sqless.sqlessmobile.network.SQLConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLSelectQuery extends SQLQuery {

    /**
     * Crea una nueva {@code SELECT} query. La query se ejecutará en un thread diferente al
     * que creó este objeto.
     *
     * @param connectionData
     * @param sql
     */
    public SQLSelectQuery(SQLConnectionManager.ConnectionData connectionData, String sql) {
        super(connectionData, sql, true);
    }

    /**
     * Crea una nueva {@code SELECT} query. El usuario tiene la opción de elegir si la query se
     * ejecutará en el mismo thread que construyó este objeto u otro nuevo.
     *
     * @param connectionData
     * @param sql
     * @param newThread
     */
    public SQLSelectQuery(SQLConnectionManager.ConnectionData connectionData, String sql, boolean newThread) {
        super(connectionData, sql, newThread);
    }

    @Override
    protected void doExecute() {
        try {
            connection = connectionData.makeConnection();
            if (connection != null) {
                statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(getSql());
                onSuccess(rs);
                querySuccess = true;
            } else {
                throw new SQLException("La conexión es nula");
            }
        } catch (SQLException e) {
            onFailure(e.getMessage());
        } finally {
            closeQuery();
        }
    }

    /**
     * Called upon successful completion of a query that returns a
     * {@link ResultSet}. This method is empty by default.
     *
     * @param rs The {@code ResultSet} resulting from this query.
     * @throws SQLException If sometime during the execution of this
     *                      method a {@code SQLException} is thrown,
     *                      {@link SQLQuery#onFailure(String)} will be called.
     */
    public void onSuccess(ResultSet rs) throws SQLException {
    }

}
