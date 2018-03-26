package com.sqless.sqlessmobile.db.queries;

import com.sqless.sqlessmobile.network.SQLConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLSelectQuery extends SQLQuery {

    public SQLSelectQuery(String sql) {
        super(sql);
    }

    /**
     * @param sql
     * @param defaultErrorHandling
     * @see SQLQuery#SQLQuery(String, boolean)
     */
    public SQLSelectQuery(String sql, boolean defaultErrorHandling) {
        super(sql, defaultErrorHandling);
    }

    @Override
    public void exec() {
        Thread execThread = new Thread(() -> {
            try {
                statement = SQLConnectionManager.getInstance().getConnection().createStatement();
                ResultSet rs = statement.executeQuery(getSql());
                onSuccess(rs);
            } catch (SQLException e) {
                if (defaultErrorHandling) {
                    onFaiureStandard(e.getMessage());
                } else {
                    onFailure(e.getMessage());
                }
            } finally {
                closeQuery();
            }
        });
        execThread.start();
    }

    @Override
    public void execOnMaster() {
        Thread execThread = new Thread(() -> {
            try {
                statement = SQLConnectionManager.getInstance().getMasterConnection().createStatement();
                ResultSet rs = statement.executeQuery(getSql());
                onSuccess(rs);
            } catch (SQLException e) {
                if (defaultErrorHandling) {
                    onFaiureStandard(e.getMessage());
                } else {
                    onFailure(e.getMessage());
                }
            } finally {
                closeQuery();
            }
        });
        execThread.start();
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
