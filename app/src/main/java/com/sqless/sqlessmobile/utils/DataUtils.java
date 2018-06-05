package com.sqless.sqlessmobile.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.widget.Toast;

import com.sqless.sqlessmobile.db.queries.SQLQuery;
import com.sqless.sqlessmobile.db.queries.SQLSelectQuery;
import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.sqlobjects.SQLObject;
import com.sqless.sqlessmobile.sqlobjects.SQLSelectable;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

public class DataUtils {
    public static void tableToJSON(Activity activity, SQLConnectionManager.ConnectionData connectionData, SQLSelectable selectable, DocumentFile file) {
        String tableName = ((SQLObject) selectable).getName();
        AsyncJSONWrite asyncJSONWrite = new AsyncJSONWrite(activity, connectionData, selectable.getSelectStatement(SQLSelectable.ALL), file);
        asyncJSONWrite.execute(tableName);
    }

    public static void resultToJSON(Activity activity, SQLConnectionManager.ConnectionData connectionData, String sql, String outputFilename, DocumentFile file) {
        AsyncJSONWrite asyncJSONWrite = new AsyncJSONWrite(activity, connectionData, sql, file);
        asyncJSONWrite.execute(outputFilename);
    }

    public static void htmlTablesToJSON(Activity activity, Map<Integer, HTMLDoc> results, DocumentFile file) {
        Toast.makeText(activity.getApplicationContext(), "Exportando " + results.size() + " resultado(s)...", Toast.LENGTH_SHORT).show();
        int entryCount = 0;
        for (Map.Entry<Integer, HTMLDoc> entry : results.entrySet()) {
            Integer resultNumber = entry.getKey();
            HTMLDoc doc = entry.getValue();
            DocumentFile outputFile = file.createFile("text/json", "Resultado_" + resultNumber + ".json");
            AsyncHtmlJSONWrite asyncHtmlJSONWrite = new AsyncHtmlJSONWrite(activity, outputFile, entryCount == results.size() - 1);
            asyncHtmlJSONWrite.execute(doc.getHTML());
            entryCount++;
        }
    }

    public static void tableToCSV(Activity activity, SQLConnectionManager.ConnectionData connectionData, SQLSelectable selectable, DocumentFile file) {
        String tableName = ((SQLObject) selectable).getName();
        AsyncCSVWrite asyncCSVWrite = new AsyncCSVWrite(activity, connectionData, selectable.getSelectStatement(SQLSelectable.ALL), file);
        asyncCSVWrite.execute(tableName);
    }

    public static void resultToCSV(Activity activity, SQLConnectionManager.ConnectionData connectionData, String sql, String outputFilename, DocumentFile file) {
        AsyncCSVWrite asyncCSVWrite = new AsyncCSVWrite(activity, connectionData, sql, file);
        asyncCSVWrite.execute(outputFilename);
    }

    public static void htmlTablesToCSV(Activity activity, Map<Integer, HTMLDoc> results, DocumentFile file) {
        Toast.makeText(activity.getApplicationContext(), "Exportando " + results.size() + " resultado(s)...", Toast.LENGTH_SHORT).show();
        int entryCount = 0;
        for (Map.Entry<Integer, HTMLDoc> entry : results.entrySet()) {
            Integer resultNumber = entry.getKey();
            HTMLDoc doc = entry.getValue();
            DocumentFile outputFile = file.createFile("text/csv", "Resultado_" + resultNumber + ".csv");
            AsyncHtmlCSVWrite asyncHtmlCSVWrite = new AsyncHtmlCSVWrite(activity, outputFile, entryCount == results.size() - 1);
            asyncHtmlCSVWrite.execute(doc.getHTML());
            entryCount++;
        }
    }

    static class AsyncHtmlCSVWrite extends AsyncTask<String, String, Void> {
        private WeakReference<Activity> activity;
        private DocumentFile file;
        private boolean showToastOnFinish;

        public AsyncHtmlCSVWrite(Activity activity, DocumentFile file, boolean showToastOnFinish) {
            this.activity = new WeakReference<>(activity);
            this.file = file;
            this.showToastOnFinish = showToastOnFinish;
        }

        @Override
        protected Void doInBackground(String... strings) {
            try (OutputStream out = activity.get().getContentResolver().openOutputStream(file.getUri())) {
                Document doc = Jsoup.parse(strings[0]);
                Elements rows = doc.select("tbody > tr");
                Elements columnNames = doc.select("th");
                boolean firstColumn = true;
                for (int i = 1; i < columnNames.size(); i++) {
                    out.write(((firstColumn ? "" : ",") + columnNames.get(i).text()).getBytes());
                    firstColumn = false;
                }
                out.write("\n".getBytes());

                for (Element row : rows) {
                    Elements columns = row.select("td");
                    boolean firstCell = true;
                    for (int i = 1; i < columns.size(); i++) {
                        out.write(((firstCell ? "" : ",") + columns.get(i).text()).getBytes());
                        firstCell = false;
                    }
                    out.write("\n".getBytes());
                }
            } catch (IOException ex) {
                Log.e("DataUtils", ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (showToastOnFinish)
                Toast.makeText(activity.get().getApplicationContext(), "Los resultados han sido exportados exitosamente", Toast.LENGTH_SHORT).show();
        }
    }

    static class AsyncHtmlJSONWrite extends AsyncTask<String, String, Void> {
        private WeakReference<Activity> activity;
        private DocumentFile file;
        private boolean showToastOnFinish;

        public AsyncHtmlJSONWrite(Activity activity, DocumentFile file, boolean showToastOnFinish) {
            this.activity = new WeakReference<>(activity);
            this.file = file;
            this.showToastOnFinish = showToastOnFinish;
        }

        @Override
        protected Void doInBackground(String... strings) {
            try (OutputStream out = activity.get().getContentResolver().openOutputStream(file.getUri())) {
                Document doc = Jsoup.parse(strings[0]);
                Elements rows = doc.select("tbody > tr");
                Elements columnNames = doc.select("th");
                out.write("[".getBytes());
                boolean firstRow = true;
                for (Element row : rows) {
                    Elements columns = row.select("td");
                    JSONObject jsonRow = new JSONObject();
                    for (int i = 1; i < columns.size(); i++) {
                        String valueStr = columns.get(i).text();
                        String columnName = columnNames.get(i).text();
                        if (valueStr != null && isNumeric(valueStr)) {
                            if (valueStr.contains(".")) {
                                jsonRow.put(columnName, Double.parseDouble(valueStr));
                            } else if (valueStr.contains("x")) {
                                jsonRow.put(columnName, Integer.parseInt(valueStr, 16));
                            } else {
                                BigInteger bigInt = new BigInteger(valueStr);
                                if (bigInt.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
                                    jsonRow.put(columnName, bigInt.toString());
                                } else {
                                    jsonRow.put(columnName, Long.parseLong(bigInt.toString()));
                                }
                            }
                        } else {
                            jsonRow.put(columnName, valueStr);
                        }
                    }
                    out.write(((firstRow ? "" : ",") + jsonRow.toString()).getBytes());
                    firstRow = false;
                }
                out.write("]".getBytes());
            } catch (IOException | JSONException ex) {
                Log.e("DataUtils", ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (showToastOnFinish)
                Toast.makeText(activity.get().getApplicationContext(), "Los resultados han sido exportados exitosamente", Toast.LENGTH_SHORT).show();
        }
    }

    static class AsyncCSVWrite extends AsyncTask<String, String, Void> {
        private WeakReference<Activity> activity;
        private SQLConnectionManager.ConnectionData connectionData;
        private DocumentFile file;
        private String sql;

        public AsyncCSVWrite(Activity activity, SQLConnectionManager.ConnectionData connectionData, String sql, DocumentFile file) {
            this.activity = new WeakReference<>(activity);
            this.connectionData = connectionData;
            this.file = file;
            this.sql = sql;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(activity.get().getApplicationContext(), "Exportando datos...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... outputFileName) {
            SQLQuery query = new SQLSelectQuery(activity.get(), connectionData, sql, false) {
                @Override
                public void onSuccess(ResultSet rs) throws SQLException {
                    DocumentFile outputFile = file.createFile("text/csv", outputFileName[0] + ".csv");
                    try (OutputStream out = activity.get().getContentResolver().openOutputStream(outputFile.getUri())) {
                        ResultSetMetaData rsmd = rs.getMetaData();
                        boolean firstCell;
                        boolean firstColumn = true;
                        int numColumns = rsmd.getColumnCount() + 1;
                        for (int i = 1; i < numColumns; i++) {
                            String columnName = rsmd.getColumnName(i);
                            out.write(((firstColumn ? "" : ",") + columnName).getBytes());
                            firstColumn = false;
                        }
                        out.write("\n".getBytes());
                        while (rs.next()) {
                            firstCell = true;
                            for (int i = 1; i < numColumns; i++) {
                                int columnType = rsmd.getColumnType(i);
                                Object obj = rs.getObject(i);
                                out.write(firstCell ? "".getBytes() : ",".getBytes());
                                firstCell = false;
                                if (obj == null) {
                                    out.write("null".getBytes());
                                    continue;
                                }
                                if (columnType == java.sql.Types.BIGINT) {
                                    out.write((rs.getInt(i) + "").getBytes());
                                } else if (columnType == java.sql.Types.BOOLEAN) {
                                    out.write((rs.getBoolean(i) + "").getBytes());
                                } else if (columnType == java.sql.Types.BLOB) {
                                    Blob blob = rs.getBlob(i);
                                    out.write(blob.getBytes(0, (int) blob.length()));
                                } else if (columnType == java.sql.Types.DOUBLE) {
                                    out.write((rs.getDouble(i) + "").getBytes());
                                } else if (columnType == java.sql.Types.FLOAT) {
                                    out.write((rs.getFloat(i) + "").getBytes());
                                } else if (columnType == java.sql.Types.INTEGER) {
                                    out.write((rs.getInt(i) + "").getBytes());
                                } else if (columnType == java.sql.Types.NVARCHAR) {
                                    out.write(rs.getNString(i).getBytes());
                                } else if (columnType == java.sql.Types.VARCHAR) {
                                    out.write(rs.getString(i).getBytes());
                                } else if (columnType == java.sql.Types.TINYINT) {
                                    out.write((rs.getInt(i) + "").getBytes());
                                } else if (columnType == java.sql.Types.SMALLINT) {
                                    out.write((rs.getInt(i) + "").getBytes());
                                } else if (columnType == java.sql.Types.DATE) {
                                    out.write((rs.getDate(i) + "").getBytes());
                                } else if (columnType == java.sql.Types.TIMESTAMP) {
                                    out.write((rs.getTimestamp(i) + "").getBytes());
                                } else {
                                    out.write((rs.getObject(i) + "").getBytes());
                                }
                            }
                            out.write("\n".getBytes());
                        }
                    } catch (IOException ex) {
                        Log.e("DataUtils", ex.getMessage());
                    }
                }
            };
            query.exec();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(activity.get().getApplicationContext(), "Los datos han sido exportados exitosamente", Toast.LENGTH_SHORT).show();
        }
    }

    static class AsyncJSONWrite extends AsyncTask<String, String, Void> {

        private WeakReference<Activity> activity;
        private SQLConnectionManager.ConnectionData connectionData;
        private DocumentFile file;
        private String sql;

        public AsyncJSONWrite(Activity activity, SQLConnectionManager.ConnectionData connectionData, String sql, DocumentFile file) {
            this.activity = new WeakReference<>(activity);
            this.connectionData = connectionData;
            this.file = file;
            this.sql = sql;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(activity.get().getApplicationContext(), "Exportando datos...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... outputFileName) {
            SQLQuery query = new SQLSelectQuery(activity.get(), connectionData, sql, false) {
                @Override
                public void onSuccess(ResultSet rs) throws SQLException {
                    DocumentFile outputFile = file.createFile("text/json", outputFileName[0] + ".json");
                    try (OutputStream out = activity.get().getContentResolver().openOutputStream(outputFile.getUri())) {
                        ResultSetMetaData rsmd = rs.getMetaData();
                        String column_name;
                        int columnType;
                        boolean firstRow = true;
                        int numColumns = rsmd.getColumnCount() + 1;
                        out.write("[".getBytes());
                        while (rs.next()) {
                            JSONObject obj = new JSONObject();
                            for (int i = 1; i < numColumns; i++) {
                                column_name = rsmd.getColumnName(i);
                                columnType = rsmd.getColumnType(i);
                                if (columnType == java.sql.Types.BIGINT) {
                                    obj.put(column_name, rs.getInt(i));
                                } else if (columnType == java.sql.Types.BOOLEAN) {
                                    obj.put(column_name, rs.getBoolean(i));
                                } else if (columnType == java.sql.Types.BLOB) {
                                    obj.put(column_name, rs.getBlob(i));
                                } else if (columnType == java.sql.Types.DOUBLE) {
                                    obj.put(column_name, rs.getDouble(i));
                                } else if (columnType == java.sql.Types.FLOAT) {
                                    obj.put(column_name, rs.getFloat(i));
                                } else if (columnType == java.sql.Types.INTEGER) {
                                    obj.put(column_name, rs.getInt(i));
                                } else if (columnType == java.sql.Types.NVARCHAR) {
                                    obj.put(column_name, rs.getNString(i));
                                } else if (columnType == java.sql.Types.VARCHAR) {
                                    obj.put(column_name, rs.getString(i));
                                } else if (columnType == java.sql.Types.TINYINT) {
                                    obj.put(column_name, rs.getInt(i));
                                } else if (columnType == java.sql.Types.SMALLINT) {
                                    obj.put(column_name, rs.getInt(i));
                                } else if (columnType == java.sql.Types.DATE) {
                                    obj.put(column_name, rs.getDate(i));
                                } else if (columnType == java.sql.Types.TIMESTAMP) {
                                    obj.put(column_name, rs.getTimestamp(i));
                                } else {
                                    obj.put(column_name, rs.getObject(i));
                                }
                            }
                            out.write(((firstRow ? "" : ",") + obj.toString()).getBytes());
                            firstRow = false;
                        }
                        out.write("]".getBytes());
                    } catch (JSONException | IOException ex) {
                        Log.e("DataUtils", ex.getMessage());
                    }
                }
            };
            query.exec();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(activity.get().getApplicationContext(), "Los datos han sido exportados exitosamente", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Checks whether a string is a valid Java number. Taken from Apache
     * NumberUtils.
     *
     * @param str
     * @return {@code true} if the string is a number.
     */
    public static boolean isNumeric(final String str) {
        if (str.isEmpty()) {
            return false;
        }
        final char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        final int start = chars[0] == '-' || chars[0] == '+' ? 1 : 0;
        if (sz > start + 1 && chars[start] == '0' && !str.contains(".")) { // leading 0, skip if is a decimal number
            if (chars[start + 1] == 'x' || chars[start + 1] == 'X') { // leading 0x/0X
                int i = start + 2;
                if (i == sz) {
                    return false; // str == "0x"
                }
                // checking hex (it can't be anything else)
                for (; i < chars.length; i++) {
                    if ((chars[i] < '0' || chars[i] > '9')
                            && (chars[i] < 'a' || chars[i] > 'f')
                            && (chars[i] < 'A' || chars[i] > 'F')) {
                        return false;
                    }
                }
                return true;
            } else if (Character.isDigit(chars[start + 1])) {
                // leading 0, but not hex, must be octal
                int i = start + 1;
                for (; i < chars.length; i++) {
                    if (chars[i] < '0' || chars[i] > '7') {
                        return false;
                    }
                }
                return true;
            }
        }
        sz--; // don't want to loop to the last char, check it afterwords
        // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || i < sz + 1 && allowSigns && !foundDigit) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                // single trailing decimal point after non-exponent is ok
                return foundDigit;
            }
            if (!allowSigns
                    && (chars[i] == 'd'
                    || chars[i] == 'D'
                    || chars[i] == 'f'
                    || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l'
                    || chars[i] == 'L') {
                // not allowing L with an exponent or decimal point
                return foundDigit && !hasExp && !hasDecPoint;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }
}
