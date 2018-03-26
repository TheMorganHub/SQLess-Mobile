package com.sqless.sqlessmobile.db.queries;

import android.util.Log;

import com.sqless.sqlessmobile.utils.SQLUtils;

import java.sql.SQLException;
import java.sql.Statement;

public abstract class SQLQuery {

    protected Statement statement;
    private String sql;
    /**
     * Si es falso, al haber un error en la query, el error no será mostrado en
     * pantalla y se asume que el usuario dio una implementación propia de
     * {@link #onFailure(String)}. Si es verdadero, SQLess mostrará un
     * diálogo de error por default con texto sacado de la excepción. <br><br>
     */
    protected boolean defaultErrorHandling;

    public SQLQuery(String sql) {
        this.sql = SQLUtils.filterDelimiterKeyword(sql);
    }

    /**
     * Inicializa una nueva query. Usar este constructor si se desea que al
     * fallar la query se muestre un mensaje por default sin tener que
     * implementar {@link #onFailure(String)}
     *
     * @param sql
     * @param defaultErrorHandling
     */
    public SQLQuery(String sql, boolean defaultErrorHandling) {
        this(sql);
        this.defaultErrorHandling = defaultErrorHandling;
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
     * Executes this query.
     */
    public abstract void exec();

    /**
     * Ejecuta la query contra la conexión master.
     */
    public void execOnMaster() {
    }

    /**
     * Closes this SQL query's {@link Statement} object.
     */
    public void closeQuery() {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
        }
    }
}
