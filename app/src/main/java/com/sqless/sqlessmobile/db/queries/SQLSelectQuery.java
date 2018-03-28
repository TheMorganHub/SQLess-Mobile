package com.sqless.sqlessmobile.db.queries;

import com.sqless.sqlessmobile.network.SQLConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLSelectQuery extends SQLQuery {

    public SQLSelectQuery(SQLConnectionManager.ConnectionData connData, String sql) {
        super(connData, sql);
    }

    public SQLSelectQuery(SQLConnectionManager.ConnectionData connData, String sql, boolean newThread) {
        super(connData, sql, newThread);
    }

    @Override
    protected void doExecute() {
        try {
            statement = SQLConnectionManager.getInstance().getConnection(connectionData).createStatement();
            ResultSet rs = statement.executeQuery(getSql());
            onSuccess(rs);
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
