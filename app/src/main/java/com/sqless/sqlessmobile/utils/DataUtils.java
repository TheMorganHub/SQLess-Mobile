package com.sqless.sqlessmobile.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.widget.Toast;

import com.sqless.sqlessmobile.db.queries.SQLQuery;
import com.sqless.sqlessmobile.db.queries.SQLSelectQuery;
import com.sqless.sqlessmobile.network.SQLConnectionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class DataUtils {
    public static void tableToJSON(Activity activity, SQLConnectionManager.ConnectionData connectionData, DocumentFile file) {
        AsyncJSONWrite asyncJSONWrite = new AsyncJSONWrite(activity, connectionData, file);
        asyncJSONWrite.execute(connectionData.getTableName());
    }

    public static void tableToCSV(Activity activity, SQLConnectionManager.ConnectionData connectionData, DocumentFile file) {
        AsyncCSVWrite asyncCSVWrite = new AsyncCSVWrite(activity, connectionData, file);
        asyncCSVWrite.execute(connectionData.getTableName());
    }

    static class AsyncCSVWrite extends AsyncTask<String, String, Void> {
        private WeakReference<Activity> activity;
        private SQLConnectionManager.ConnectionData connectionData;
        private DocumentFile file;

        public AsyncCSVWrite(Activity activity, SQLConnectionManager.ConnectionData connectionData, DocumentFile file) {
            this.activity = new WeakReference<>(activity);
            this.connectionData = connectionData;
            this.file = file;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(activity.get().getApplicationContext(), "Exportando datos en tarea secundaria...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            SQLQuery query = new SQLSelectQuery(connectionData, "SELECT * FROM " + connectionData.getTableName(), false) {
                @Override
                public void onSuccess(ResultSet rs) throws SQLException {
                    try (OutputStream out = activity.get().getContentResolver().openOutputStream(file.getUri())) {
                        ResultSetMetaData rsmd = rs.getMetaData();
                        boolean firstCell;
                        boolean firstColumn = true;
                        int numColumns = rsmd.getColumnCount() + 1;
                        for (int i = 1; i < numColumns; i++) {
                            String columnName = rsmd.getColumnName(i);
                            out.write(firstColumn ? columnName.getBytes() : ("," + columnName).getBytes());
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
            Toast.makeText(activity.get().getApplicationContext(), "La tabla " + connectionData.getTableName() + " ha sido exportada exitosamente", Toast.LENGTH_SHORT).show();
        }
    }

    static class AsyncJSONWrite extends AsyncTask<String, String, Void> {

        private WeakReference<Activity> activity;
        private SQLConnectionManager.ConnectionData connectionData;
        private DocumentFile file;

        public AsyncJSONWrite(Activity activity, SQLConnectionManager.ConnectionData connectionData, DocumentFile file) {
            this.activity = new WeakReference<>(activity);
            this.connectionData = connectionData;
            this.file = file;
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(activity.get().getApplicationContext(), "Exportando datos en tarea secundaria...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... tableName) {
            SQLQuery query = new SQLSelectQuery(connectionData, "SELECT * FROM " + connectionData.getTableName(), false) {
                @Override
                public void onSuccess(ResultSet rs) throws SQLException {
                    try (OutputStream out = activity.get().getContentResolver().openOutputStream(file.getUri())) {
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
                            out.write(firstRow ? obj.toString().getBytes() : ("," + obj.toString()).getBytes());
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
            Toast.makeText(activity.get().getApplicationContext(), "La tabla " + connectionData.getTableName() + " ha sido exportada exitosamente", Toast.LENGTH_SHORT).show();
        }
    }
}
