package com.sqless.sqlessmobile.db.queries;

import android.util.Log;

import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.utils.SQLUtils;
import com.sqless.sqlessmobile.utils.UIUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class SQLQuery {

    protected Statement statement;
    private String sql;
    protected boolean newThread;
    protected SQLConnectionManager.ConnectionData connectionData;
    /**
     * Denota si la ejecución de una query fue exitosa.
     */
    protected boolean querySuccess;
    protected Connection connection;

    public SQLQuery(SQLConnectionManager.ConnectionData conData, String sql) {
        this.sql = SQLUtils.filterDelimiterKeyword(sql);
        this.connectionData = conData;
        querySuccess = false;
    }

    public SQLQuery(SQLConnectionManager.ConnectionData conData, String sql, boolean newThread) {
        this(conData, sql);
        this.newThread = newThread;
    }

    public String getSql() {
        return sql;
    }

    /**
     * Called upon execution failure of a query.
     * This method is empty by default. Children are free to override
     * it as they please.
     *
     * @param errMessage The error message produced by the SQL engine.
     */
    public void onFailure(String errMessage) {
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
     * Se ejecutará este método luego de que la conexión de esta query muera SÓLO si la query fue exitosa.
     * Es decir, este método sólo se ejecutará si {@link #querySuccess} fue seteado {@code true}.
     */
    public void onConnectionKilled() {
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
                connection.close();
                connection = null;
                Log.i("SQLQuery", "Killed query connection.");
                if (querySuccess) {
                    UIUtils.invokeOnUIThread(this::onConnectionKilled);
                }
            }
        } catch (SQLException e) {
            Log.e("SQLQuery", "No se pudo matar la conexión");
        }
    }
}
