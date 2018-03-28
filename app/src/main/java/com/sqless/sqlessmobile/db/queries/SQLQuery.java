package com.sqless.sqlessmobile.db.queries;

import android.util.Log;

import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.utils.SQLUtils;

import java.sql.SQLException;
import java.sql.Statement;

public abstract class SQLQuery {

    protected Statement statement;
    private String sql;
    protected boolean newThread;
    protected SQLConnectionManager.ConnectionData connectionData;

    public SQLQuery(SQLConnectionManager.ConnectionData conData, String sql) {
        this.sql = SQLUtils.filterDelimiterKeyword(sql);
        this.connectionData = conData;
    }

    public SQLQuery(SQLConnectionManager.ConnectionData conData, String sql, boolean newThread) {
        this(conData, sql);
        this.newThread = newThread;

    }

    public String getSql() {
        return sql;
    }

    /**
     * Called upon execution failure of a query IF {@code defaultErrorHandling}
     * is false. This method is empty by default. Children are free to override
     * it as they please.
     *
     * @param errMessage The error message produced by the SQL engine.
     */
    public void onFailure(String errMessage) {
    }

    public void onFaiureStandard(String errMessage) {
        Log.e("Error", errMessage);
    }

    /**
     * Ejecuta esta query. Si el valor de {@link #newThread} es falso, la query se ejecutará en el
     * mismo thread que ha creado este objeto. De lo contrario, se creará un nuevo thread para esa query.
     *
     * @see #doExecute()
     */
    public void exec() {
        if (newThread) {
            Thread execThread = new Thread(this::doExecute);
            execThread.start();
        } else {
            doExecute();
        }
    }

    /**
     * Contiene la lógica de ejecución de la query. El manejo de threads se hace en {@link #exec()}.
     */
    protected abstract void doExecute();

    /**
     * Closes this SQL query's {@link Statement} object.
     */
    public void closeQuery() {
        try {
            if (statement != null) {
                statement.close();
                connectionData.killConnectionIfActive();
            }
        } catch (SQLException e) {
        }
    }
}
