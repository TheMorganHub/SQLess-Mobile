package com.sqless.sqlessmobile.db.queries;

import com.sqless.sqlessmobile.network.SQLConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLSelectQuery extends SQLQuery {

    public SQLSelectQuery(SQLConnectionManager.ConnectionData connectionData, String sql) {
        super(connectionData, sql);
    }

    public SQLSelectQuery(SQLConnectionManager.ConnectionData connectionData, String sql, boolean newThread) {
        super(connectionData, sql, newThread);
    }

    @Override
    protected void doExecute() {
        try {
            Connection conFromData = SQLConnectionManager.getInstance().getConnection(connectionData);
            if (conFromData != null) {
                statement = conFromData.createStatement();
                ResultSet rs = statement.executeQuery(getSql());
                onSuccess(rs);
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
