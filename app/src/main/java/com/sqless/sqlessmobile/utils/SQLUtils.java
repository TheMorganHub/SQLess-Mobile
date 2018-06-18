package com.sqless.sqlessmobile.utils;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.sqless.sqlessmobile.db.queries.SQLQuery;
import com.sqless.sqlessmobile.db.queries.SQLSelectQuery;
import com.sqless.sqlessmobile.db.queries.SQLUpdateQuery;
import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.sqlobjects.SQLColumn;
import com.sqless.sqlessmobile.sqlobjects.SQLDroppable;
import com.sqless.sqlessmobile.sqlobjects.SQLExecutable;
import com.sqless.sqlessmobile.sqlobjects.SQLFunction;
import com.sqless.sqlessmobile.sqlobjects.SQLObject;
import com.sqless.sqlessmobile.sqlobjects.SQLParameter;
import com.sqless.sqlessmobile.sqlobjects.SQLProcedure;
import com.sqless.sqlessmobile.sqlobjects.SQLRenameable;
import com.sqless.sqlessmobile.sqlobjects.SQLTable;
import com.sqless.sqlessmobile.sqlobjects.SQLView;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLUtils {

    public static boolean datatypeIsSupported(String datatype) {
        return datatype.equals("int") || datatype.equals("varchar") || datatype.equals("datetime") || datatype.equals("decimal");
    }

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

    public static void insertIntoTable(Activity context, SQLConnectionManager.ConnectionData connectionData, SQLTable sqlTable, Map<String, String> data,
                                       Runnable callbackSuccess, Callback<String> callbackFailure) {
        SQLQuery insertIntoQuery = new SQLUpdateQuery(context, connectionData, sqlTable.getInsertIntoStatement(data)) {
            @Override
            public void onSuccess(int updateCount) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, callbackSuccess);
            }

            @Override
            public void onFailure(String errMessage) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackFailure.exec(errMessage));
            }
        };
        insertIntoQuery.exec();
    }

    /**
     * Devuelve los nombres de las bases de datos en el motor.
     *
     * @param callbackSuccess El callback que se ejecutará si la consulta es exitosa. Al callback
     *                        se le pasará como parámetro una lista con los nombres de las bases de datos. Este mismo se ejecutará en el thread de UI.
     */
    public static void getDatabaseNames(Activity context, SQLConnectionManager.ConnectionData connectionData, Callback<List<String>> callbackSuccess) {
        SQLQuery nameQuery = new SQLSelectQuery(context, connectionData, "SHOW DATABASES") {
            @Override
            public void onSuccess(ResultSet rs) throws SQLException {
                List<String> names = new ArrayList<>();
                while (rs.next()) {
                    names.add(rs.getString(1));
                }
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackSuccess.exec(names));
            }

            @Override
            public void onFailure(String errMessage) {
                Log.e("Err", errMessage);
            }
        };
        nameQuery.exec();
    }

    public static void getTables(Activity context, SQLConnectionManager.ConnectionData connectionData, Callback<List<SQLTable>> callbackSuccess, Callback<String> callbackFailure) {
        SQLQuery tablesQuery = new SQLSelectQuery(context, connectionData, "show full tables where Table_Type = 'BASE TABLE' OR Table_Type = 'SYSTEM VIEW'") {
            @Override
            public void onSuccess(ResultSet rs) throws SQLException {
                List<SQLTable> tableNames = new ArrayList<>();
                while (rs.next()) {
                    tableNames.add(new SQLTable(rs.getString(1)));
                }
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackSuccess.exec(tableNames));
            }

            @Override
            public void onFailure(String errMessage) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackFailure.exec(errMessage));
            }
        };
        tablesQuery.exec();
    }

    public static void getViews(Activity context, SQLConnectionManager.ConnectionData connectionData, Callback<List<SQLView>> callbackSuccess, Callback<String> callbackFailure) {
        SQLQuery viewsQuery = new SQLSelectQuery(context, connectionData, "SHOW FULL TABLES IN " + connectionData.database + " WHERE TABLE_TYPE LIKE 'VIEW'") {
            @Override
            public void onSuccess(ResultSet rs) throws SQLException {
                List<SQLView> views = new ArrayList<>();
                while (rs.next()) {
                    views.add(new SQLView(rs.getString(1)));
                }
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackSuccess.exec(views));
            }

            @Override
            public void onFailure(String errMessage) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackFailure.exec(errMessage));
            }
        };
        viewsQuery.exec();
    }

    public static void getColumnNamesInTable(Activity context, SQLConnectionManager.ConnectionData connectionData, String tableName, Callback<List<String>> callbackSuccess, Callback<String> callbackFailure) {
        SQLQuery columnNamesQuery = new SQLSelectQuery(context, connectionData, "SHOW COLUMNS FROM " + tableName) {
            @Override
            public void onSuccess(ResultSet rs) throws SQLException {
                List<String> columnNames = new ArrayList<>();
                while (rs.next()) {
                    columnNames.add(rs.getString("Field"));
                }
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackSuccess.exec(columnNames));
            }

            @Override
            public void onFailure(String errMessage) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackFailure.exec(errMessage));
            }
        };
        columnNamesQuery.exec();
    }

    public static void getViewColumns(Activity context, SQLObject selectable, SQLConnectionManager.ConnectionData connectionData, Callback<List<SQLColumn>> callbackSuccess, Callback<String> callbackFailure) {
        SQLQuery viewColumnsQuery = new SQLSelectQuery(context, connectionData, "SELECT * FROM information_schema.`COLUMNS` WHERE TABLE_SCHEMA = " +
                "'" + connectionData.database + "' AND TABLE_NAME = '" + selectable.getName() + "'") {
            @Override
            public void onSuccess(ResultSet rs) throws SQLException {
                List<SQLColumn> columns = new ArrayList<>();
                while (rs.next()) {
                    String colName = rs.getString("COLUMN_NAME");
                    String dataType = rs.getString("DATA_TYPE");
                    SQLColumn column = new SQLColumn(colName, selectable.getName(), dataType);
                    columns.add(column);
                }
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackSuccess.exec(columns));
            }

            @Override
            public void onFailure(String errMessage) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackFailure.exec(errMessage));
            }
        };
        viewColumnsQuery.exec();
    }

    public static void getColumns(Activity context, SQLObject sqlTable, SQLConnectionManager.ConnectionData connectionData, Callback<List<SQLColumn>> callbackSuccess, Callback<String> callbackFailure) {
        SQLQuery columnsQuery = new SQLSelectQuery(context, connectionData,
                "SELECT * FROM information_schema.`COLUMNS` WHERE TABLE_SCHEMA = '" + connectionData.database
                        + "' AND TABLE_NAME = '" + sqlTable.getName() + "'") {
            @Override
            public void onSuccess(ResultSet rs) throws SQLException {
                List<SQLColumn> columns = new ArrayList<>();
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    String dataType = rs.getString("DATA_TYPE");
                    SQLColumn column = new SQLColumn(columnName, sqlTable.getName(), dataType);
                    column.setLength(rs.getString("CHARACTER_MAXIMUM_LENGTH"));
                    column.setNumericPrecision(rs.getString("NUMERIC_PRECISION"));
                    column.setNumericScale(rs.getString("NUMERIC_SCALE"));
                    column.setNullable(rs.getString("IS_NULLABLE").equals("YES"));
                    column.setIsPK(rs.getString("COLUMN_KEY").equals("PRI"));
                    column.setDefaultVal(rs.getString("COLUMN_DEFAULT"));
                    column.setCharacterSet(rs.getString("CHARACTER_SET_NAME"));
                    column.setCollation(rs.getString("COLLATION_NAME"));
                    column.setUnsigned(rs.getString("COLUMN_TYPE").endsWith("unsigned"), false);
                    column.setDateTimePrecision(DataTypeUtils.dataTypeIsTimeBased(dataType));
                    column.setOnUpdateCurrentTimeStamp(rs.getString("EXTRA").equalsIgnoreCase("ON UPDATE CURRENT_TIMESTAMP"));
                    column.setAutoincrement(rs.getString("EXTRA").equals("auto_increment"));
                    columns.add(column);
                    if (column.getDatatype().equals("enum")) {
                        String enumTypes = rs.getString("COLUMN_TYPE");
                        column.setEnumLikeValues(enumTypes.substring(5, enumTypes.length() - 1));
                    } else if (column.getDatatype().equals("set")) {
                        String setTypes = rs.getString("COLUMN_TYPE");
                        column.setEnumLikeValues(setTypes.substring(4, setTypes.length() - 1));
                    }
                }


                SQLQuery fkQuery = new SQLSelectQuery(context, connectionData, "SELECT DISTINCT COLUMN_NAME\n" +
                        "FROM information_schema.TABLE_CONSTRAINTS i\n" +
                        "LEFT JOIN information_schema.KEY_COLUMN_USAGE k ON i.CONSTRAINT_NAME = k.CONSTRAINT_NAME\n" +
                        "LEFT JOIN information_schema.REFERENTIAL_CONSTRAINTS r ON r.CONSTRAINT_NAME = k.CONSTRAINT_NAME\n" +
                        "WHERE i.CONSTRAINT_TYPE = 'FOREIGN KEY'\n" +
                        "AND i.TABLE_SCHEMA = '" + connectionData.database + "'\n" +
                        "AND i.TABLE_NAME = '" + sqlTable.getName() + "';", false) {
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
                        UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackSuccess.exec(columns));
                    }
                };
                fkQuery.exec();
            }

            @Override
            public void onFailure(String errMessage) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackFailure.exec(errMessage));
            }
        };
        columnsQuery.exec();
    }

    public static void updateColumn(Activity context, SQLConnectionManager.ConnectionData connectionData, SQLColumn column, Runnable callbackSuccess, Callback<String> callbackFailure) {
        SQLQuery updateColumn = new SQLUpdateQuery(context, connectionData, column.getChangeColumnStatement()) {
            @Override
            public void onSuccess(int updateCount) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, callbackSuccess);
            }

            @Override
            public void onFailure(String errMessage) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackFailure.exec(errMessage));
            }
        };
        updateColumn.exec();
    }

    public static void getDbCollationAndCharSetName(Activity context, SQLConnectionManager.ConnectionData connectionData, Callback<Map<String, String>> callbackSuccess) {
        Map<String, String> collationAndCharset = new HashMap<>();
        SQLQuery tableCollationQuery = new SQLSelectQuery(context, connectionData, "SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME FROM information_schema.SCHEMATA\n"
                + "WHERE schema_name = '" + connectionData.database + "'") {
            @Override
            public void onSuccess(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    collationAndCharset.put("collation", rs.getString("DEFAULT_COLLATION_NAME"));
                    collationAndCharset.put("charset", rs.getString("DEFAULT_CHARACTER_SET_NAME"));
                }
                callbackSuccess.exec(collationAndCharset);
            }
        };
        tableCollationQuery.exec();
    }

    public static void getExecutables(Activity context, SQLConnectionManager.ConnectionData conData, Class<? extends SQLExecutable> className,
                                      Callback<List<SQLExecutable>> callbackSuccess, Callback<String> callbackFailure) {
        List<SQLExecutable> executables = new ArrayList<>();
        SQLQuery getFunctionsQuery = new SQLSelectQuery(context, conData, "SHOW" + (className == SQLFunction.class ? " FUNCTION " : " PROCEDURE ") + "STATUS WHERE Db = '" + conData.database + "'") {
            @Override
            public void onSuccess(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    String name = rs.getString("Name");
                    executables.add(className == SQLFunction.class ? new SQLFunction(name) : new SQLProcedure(name));
                }
                if (!executables.isEmpty()) {
                    SQLQuery getParametersQuery = new SQLSelectQuery(context, conData, "SELECT SPECIFIC_NAME, PARAMETER_NAME, DATA_TYPE FROM information_schema.parameters\n"
                            + "WHERE SPECIFIC_SCHEMA = '" + conData.database + "' AND ROUTINE_TYPE = '"
                            + (className == SQLFunction.class ? "FUNCTION" : "PROCEDURE") + "' AND PARAMETER_NAME IS NOT NULL", false) {
                        @Override
                        public void onSuccess(ResultSet rs) throws SQLException {
                            String previousName = null;
                            SQLExecutable executable = null;
                            while (rs.next()) {
                                String specificName = rs.getString("SPECIFIC_NAME");
                                if (!specificName.equals(previousName)) {
                                    for (SQLExecutable e : executables) {
                                        if (e.getName().equals(specificName)) {
                                            executable = e;
                                            break;
                                        }
                                    }
                                }
                                String paramName = rs.getString("PARAMETER_NAME");
                                String dataType = rs.getString("DATA_TYPE");
                                if (executable != null) {
                                    executable.addParameter(new SQLParameter(paramName, dataType));
                                }
                                previousName = specificName;
                            }
                        }
                    };
                    getParametersQuery.exec();
                }
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackSuccess.exec(executables));
            }

            @Override
            public void onFailure(String errMessage) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackFailure.exec(errMessage));
            }
        };
        getFunctionsQuery.exec();
    }

    public static void dropEntity(Activity context, SQLConnectionManager.ConnectionData connData, SQLDroppable droppable, Runnable callbackSuccess, Callback<String> callbackFailure) {
        SQLQuery dropQuery = new SQLUpdateQuery(context, connData, droppable.getDropStatement()) {
            @Override
            public void onSuccess(int updateCount) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, callbackSuccess::run);
            }

            @Override
            public void onFailure(String errMessage) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackFailure.exec(errMessage));
            }
        };
        dropQuery.exec();
    }

    public static void renameEntity(Activity context, SQLConnectionManager.ConnectionData connData, String newName, SQLRenameable renameable, Runnable callbackSuccess, Callback<String> callbackFailure) {
        SQLQuery renameQuery = new SQLUpdateQuery(context, connData, renameable.getRenameStatement(newName)) {
            @Override
            public void onSuccess(int updateCount) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, callbackSuccess::run);
            }

            @Override
            public void onFailure(String errMessage) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackFailure.exec(errMessage));
            }
        };
        renameQuery.exec();
    }

    public static void createHTMLFromQueryResult(Activity context, SQLConnectionManager.ConnectionData connData, String sql, Callback<HTMLDoc> callbackSuccess, Callback<String> callbackFailure) {
        SQLQuery tableQuery = new SQLSelectQuery(context, connData, sql) {
            @Override
            public void onSuccess(ResultSet rs) throws SQLException {
                HTMLDoc doc = createHTMLFromResultSet(rs);
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackSuccess.exec(doc));
            }

            @Override
            public void onFailure(String errMessage) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> callbackFailure.exec(errMessage));
            }
        };
        tableQuery.exec();
    }

    public static HTMLDoc createHTMLFromResultSet(ResultSet resultSet) throws SQLException {
        HTMLDoc.HTMLDocBuilder builder = new HTMLDoc.HTMLDocBuilder("tablehtml")
                .withCss("style");

        builder.addHTML("<table>");
        builder.addHTML("<thead>");
        builder.addHTML("<tr>");

        List<String> tableHeaders = getTableHeaders(resultSet.getMetaData());
        builder.addHTML("<th>").addHTML("</th>"); //numero de filas
        for (String header : tableHeaders) {
            builder.addHTML("<th>").addHTML(header).addHTML("</th>");
        }
        builder.addHTML("</tr>");
        builder.addHTML("</thead>");
        builder.addHTML("<tbody>");

        int rowCount = 0;
        while (resultSet.next()) {
            builder.addHTML("<tr>");
            builder.addHTML("<td>").addHTML(++rowCount).addHTML("</td>");
            for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                builder.addHTML(getHTMLForValue(resultSet.getMetaData().getColumnTypeName(i), resultSet, i));
            }
            builder.addHTML("</tr>");
        }
        builder.addHTML("</tbody>");
        builder.addHTML("</table>");

        return builder.build();
    }

    private static String getHTMLForValue(String columnTypeName, ResultSet rs, int col) throws SQLException {
        if (rs.getString(col) == null) {
            return "<td class=\"null-value\">(Null)</td>";
        }
        if (columnTypeName.equals("BLOB")) {
            return "<td>" + DataTypeUtils.parseBlob(rs.getBlob(col)) + "</td>";
        }

        return "<td>" + rs.getString(col) + "</td>";
    }


    private static List<String> getTableHeaders(ResultSetMetaData rsmd) throws SQLException {
        List<String> tableHeaders = new ArrayList<>();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            tableHeaders.add(rsmd.getColumnName(i));
        }
        return tableHeaders;
    }
}
