package com.sqless.sqlessmobile.utils;

import android.text.TextUtils;
import android.util.Log;

import com.sqless.sqlessmobile.db.queries.SQLQuery;
import com.sqless.sqlessmobile.db.queries.SQLSelectQuery;
import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.sqlobjects.SQLColumn;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLUtils {

    /**
     * Filtra la palabra clave 'DELIMITER' y todos los delimitadores que no sean
     * ';' asociados a esa palabra clave en la query dada ya que no es válida en
     * JDBC.<br>
     * Por ejemplo: <br><pre>
     * <code>
     * DELIMITER $$ <br>
     * <p>
     * CREATE PROCEDURE film_not_in_stock(IN p_film_id INT, IN p_store_id INT,
     * OUT p_film_count INT) READS SQL DATA <br>
     * BEGIN <br>
     * SELECT inventory_id FROM inventory WHERE film_id = p_film_id AND store_id
     * = p_store_id AND NOT inventory_in_stock(inventory_id); <br>
     * <p>
     * SELECT FOUND_ROWS() INTO p_film_count; <br>
     * END $$
     * </code></pre>
     * <br>
     * <br>
     * Se convertirá en: <br>
     * <pre><code>
     * CREATE PROCEDURE film_not_in_stock(IN p_film_id INT, IN p_store_id INT,
     * OUT p_film_count INT) READS SQL DATA <br>
     * BEGIN <br>
     * SELECT inventory_id FROM inventory WHERE film_id = p_film_id AND store_id
     * = p_store_id AND NOT inventory_in_stock(inventory_id); <br>
     *
     * SELECT FOUND_ROWS() INTO p_film_count; <br>
     * END ;
     * </code></pre>
     *
     * @param sql La query SQL a filtrar.
     * @return La misma query SQL sin nada relacionado a {@code DELIMITER}. Si
     * la query original no hace uso de la palabra clave {@code DELIMITER} será
     * retornada sin modificaciones.
     */
    public static String filterDelimiterKeyword(String sql) {
        Pattern delimiterPat = Pattern.compile("DELIMITER *(\\S*)");
        Matcher matcher = delimiterPat.matcher(sql);
        Set<String> delimitersToReplace = new HashSet<>();
        while (matcher.find()) {
            String delimiter = matcher.group(1);
            if (!delimiter.equals(";")) {
                //delimitadores que necesitan ser escapados antes de ser aplicados en expresiones regulares
                if (delimiter.contains("/") || delimiter.contains("$")) {
                    StringBuilder escapedDelimiters = new StringBuilder();
                    for (int i = 0; i < delimiter.length(); i++) {
                        if (delimiter.charAt(i) == '/' || delimiter.charAt(i) == '$') {
                            escapedDelimiters.append('\\').append(delimiter.charAt(i));
                        }
                    }
                    delimiter = escapedDelimiters.toString();
                }
                delimitersToReplace.add(delimiter);
            }
        }

        if (delimitersToReplace.isEmpty()) {
            return sql;
        }
        String replaced = sql.replaceAll(TextUtils.join("|", delimitersToReplace), ";");
        return replaced.replaceAll("DELIMITER *(\\S*)", "");
    }

    /**
     * Devuelve los nombres de las bases de datos en el motor.
     *
     * @param callbackSuccess El callback que se ejecutará si la consulta es exitosa. Al callback
     *                        se le pasará como parámetro una lista con los nombres de las bases de datos. Este mismo se ejecutará en el thread de UI.
     */
    public static void getDatabaseNames(SQLConnectionManager.ConnectionData connectionData, Callback<List<String>> callbackSuccess) {
        SQLQuery nameQuery = new SQLSelectQuery(connectionData, "SHOW DATABASES") {
            @Override
            public void onSuccess(ResultSet rs) throws SQLException {
                List<String> names = new ArrayList<>();
                while (rs.next()) {
                    names.add(rs.getString(1));
                }
                UIUtils.invokeLaterOnUI(() -> callbackSuccess.exec(names));
            }

            @Override
            public void onFailure(String errMessage) {
                Log.e("Err", errMessage);
            }
        };
        nameQuery.exec();
    }

    public static void getTableNames(SQLConnectionManager.ConnectionData connectionData, Callback<List<String>> callbackSuccess, Callback<String> callbackFailure) {
        SQLQuery tablesQuery = new SQLSelectQuery(connectionData, "show full tables where Table_Type = 'BASE TABLE' OR Table_Type = 'SYSTEM VIEW'") {
            @Override
            public void onSuccess(ResultSet rs) throws SQLException {
                List<String> tableNames = new ArrayList<>();
                while (rs.next()) {
                    tableNames.add(rs.getString(1));
                }
                UIUtils.invokeLaterOnUI(() -> callbackSuccess.exec(tableNames));
            }

            @Override
            public void onFailure(String errMessage) {
                UIUtils.invokeLaterOnUI(() -> callbackFailure.exec(errMessage));
            }
        };
        tablesQuery.exec();
    }

    public static void getColumnNamesInTable(SQLConnectionManager.ConnectionData connectionData, String tableName, Callback<List<String>> callbackSuccess, Callback<String> callbackFailure) {
        SQLQuery columnNamesQuery = new SQLSelectQuery(connectionData, "SHOW COLUMNS FROM " + tableName) {
            @Override
            public void onSuccess(ResultSet rs) throws SQLException {
                List<String> columnNames = new ArrayList<>();
                while (rs.next()) {
                    columnNames.add(rs.getString("Field"));
                }
                UIUtils.invokeLaterOnUI(() -> callbackSuccess.exec(columnNames));
            }

            @Override
            public void onFailure(String errMessage) {
                UIUtils.invokeLaterOnUI(() -> callbackFailure.exec(errMessage));
            }
        };
        columnNamesQuery.exec();
    }

    public static void getColumns(SQLConnectionManager.ConnectionData connectionData, Callback<List<SQLColumn>> callbackSuccess, Callback<String> callbackFailure) {
        SQLQuery columnsQuery = new SQLSelectQuery(connectionData,
                "SELECT COLUMN_NAME, DATA_TYPE, COLUMN_KEY FROM information_schema.`COLUMNS` WHERE TABLE_SCHEMA = '" + connectionData.database
                        + "' AND TABLE_NAME = '" + connectionData.getTableName() + "'") {
            @Override
            public void onSuccess(ResultSet rs) throws SQLException {
                List<SQLColumn> columns = new ArrayList<>();
                while (rs.next()) {
                    String colName = rs.getString("COLUMN_NAME");
                    String dataType = rs.getString("DATA_TYPE");
                    SQLColumn column = new SQLColumn(colName, dataType);
                    column.setIsPK(rs.getString("COLUMN_KEY").equals("PRI"));
                    columns.add(column);
                }

                SQLQuery fkQuery = new SQLSelectQuery(connectionData, "SELECT DISTINCT COLUMN_NAME\n" +
                        "FROM information_schema.TABLE_CONSTRAINTS i\n" +
                        "LEFT JOIN information_schema.KEY_COLUMN_USAGE k ON i.CONSTRAINT_NAME = k.CONSTRAINT_NAME\n" +
                        "LEFT JOIN information_schema.REFERENTIAL_CONSTRAINTS r ON r.CONSTRAINT_NAME = k.CONSTRAINT_NAME\n" +
                        "WHERE i.CONSTRAINT_TYPE = 'FOREIGN KEY'\n" +
                        "AND i.TABLE_SCHEMA = '" + connectionData.database + "'\n" +
                        "AND i.TABLE_NAME = '" + connectionData.getTableName() + "';", false) {
                    @Override
                    public void onSuccess(ResultSet rs) throws SQLException {
                        while (rs.next()) {
                            String colFkName = rs.getString(1);
                            for (SQLColumn column : columns) {
                                if (column.getName().equals(colFkName)) {
                                    column.setIsFK(true);
                                    break;
                                }
                            }
                        }
                    }
                };
                fkQuery.exec();
                UIUtils.invokeLaterOnUI(() -> callbackSuccess.exec(columns));
            }

            @Override
            public void onFailure(String errMessage) {
                UIUtils.invokeLaterOnUI(() -> callbackFailure.exec(errMessage));
            }
        };
        columnsQuery.exec();
    }
}
