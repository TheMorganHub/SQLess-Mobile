package com.sqless.sqlessmobile.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sqless.sqlessmobile.network.SQLConnectionManager;

import java.util.ArrayList;
import java.util.List;

public class HelperDB extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "DBConnections";
    private String errLogs;

    public HelperDB(Context contexto) {
        super(contexto, DB_NAME, null, DB_VERSION);
    }

    public void onCreate(SQLiteDatabase bd) {
        bd.execSQL("CREATE TABLE \"connections\" (\n" +
                "\"id\"  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                "\"host\"  TEXT NOT NULL,\n" +
                "\"port\"  TEXT NOT NULL,\n" +
                "\"username\"  TEXT NOT NULL,\n" +
                "\"password\"  TEXT NOT NULL\n" +
                ");");
    }

    public int count(String nombreTabla) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + nombreTabla, null);
        int count = c.moveToFirst() ? c.getInt(0) : 0;
        c.close();
        return count;
    }

    public List<SQLConnectionManager.ConnectionData> getConnections() {
        List<SQLConnectionManager.ConnectionData> connections = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM connections", null);
        if (c.moveToFirst()) {
            do {
                String host = c.getString(c.getColumnIndex("host"));
                String port = c.getString(c.getColumnIndex("port"));
                String username = c.getString(c.getColumnIndex("username"));
                String password = c.getString(c.getColumnIndex("password"));
                connections.add(new SQLConnectionManager.ConnectionData(host, port, username, password));
            } while (c.moveToNext());
        }
        c.close();
        return connections;
    }

    public long insertConnection(SQLConnectionManager.ConnectionData connectionData) {
        ContentValues values = new ContentValues();
        values.put("host", connectionData.host);
        values.put("port", connectionData.port);
        values.put("username", connectionData.username);
        values.put("password", connectionData.password);
        return getWritableDatabase().insert("connections", null, values);
    }

    public String getErrLogs() {
        return errLogs;
    }

    /**
     * Imprime el contenido de una tabla dada. Este es un método de conveniencia.
     *
     * @param tabla El nombre de la tabla.
     */
    public void logTable(String tabla) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM " + tabla, null);
        int colCount = c.getColumnCount();

        if (c.moveToFirst()) {
            do {
                StringBuilder fila = new StringBuilder();
                for (int i = 0; i < colCount; i++) {
                    fila.append(c.getString(i)).append(", ");
                }
                fila.append("\n");
                Log.i("HelperDB", fila.toString());
            } while (c.moveToNext());
        }
        c.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int vAnt, int vNueva) {
//        if (vNueva < 3) {
//            db.execSQL("CREATE TABLE Login (id INTEGER, username TEXT, password TEXT)");
//            db.execSQL("INSERT INTO Login (id, username, password) VALUES(1, 'David', '1234')");
//        }
        Log.i("DBMSJ", "Base de datos actualizada a version " + vNueva);
    }

    /**
     * Si no overrideamos este método, no nos va a dejar downgradear porque la implementación
     * original tira una excepcion como default.
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("DBMSJ", "Base de datos downgradeada a version " + newVersion);
    }


}