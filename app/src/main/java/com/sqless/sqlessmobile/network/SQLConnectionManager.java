package com.sqless.sqlessmobile.network;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.adapters.Subtitulado;
import com.sqless.sqlessmobile.utils.Callback;
import com.sqless.sqlessmobile.utils.SQLUtils;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnectionManager {

    private static SQLConnectionManager INSTANCE;
    private ConnectionData lastSuccessful;

    private SQLConnectionManager() {
    }

    /**
     * Tests a specified activeConnection by attempting to connect to database
     * "master" in SQLServer using a temporary {@code Connection} object. If
     * successful, the {@code Connection} is then promptly closed.
     *
     * @param v
     * @param username
     * @param password
     * @param hostName
     * @param port
     * @param callbackSuccess
     * @param callbackFailure
     */
    public void testConnection(View v, String username, String password, String hostName, String port, Callback<ConnectionData> callbackSuccess, Callback<String> callbackFailure) {
        final ConstraintLayout layProgress = v.findViewById(R.id.lay_progress_bar);
        final ConstraintLayout layInner = v.findViewById(R.id.lay_inner);
        if (layProgress != null && layInner != null) {
            layInner.setVisibility(View.INVISIBLE);
            layProgress.setVisibility(View.VISIBLE);
        }
        Thread conTestThread = new Thread(() -> {
            try (Connection testCon = DriverManager.getConnection("jdbc:drizzle://" + hostName + ":" + port + "/mysql?connectTimeout=3", username, password)) {
                lastSuccessful = new ConnectionData(hostName, port, "mysql", username, password);

                SQLUtils.getDatabaseNames(lastSuccessful, names -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_list_item_1, names);
                    ((Spinner) v.findViewById(R.id.spinner_dbs)).setAdapter(adapter);
                    v.findViewById(R.id.spinner_dbs).setVisibility(View.VISIBLE);
                });
                v.post(() -> callbackSuccess.exec(lastSuccessful));
            } catch (SQLException e) {
                Log.e("SQLConnectionManager", "Test failed");
                Log.e("SQLConnectionManager", e.getMessage());
                v.post(() -> callbackFailure.exec(e.getMessage()));
            } finally {
                v.post(() -> {
                    if (layProgress != null && layInner != null) {
                        layProgress.setVisibility(View.INVISIBLE);
                        layInner.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        conTestThread.start();
    }


    public ConnectionData getLastSuccessful() {
        return lastSuccessful;
    }

    public Connection getConnection(ConnectionData data) {
        return data.makeConnection();
    }

    public static SQLConnectionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SQLConnectionManager();
        }
        return INSTANCE;
    }

    public static class ConnectionData implements Subtitulado, Serializable {

        private long id;
        public String host;
        public String port;
        public String database;
        public String username;
        public String password;
        private String tableName;
        private Connection connection;

        public ConnectionData(long id, String host, String port, String database, String username, String password) {
            this(host, port, database, username, password);
            this.id = id;
            this.host = host;
            this.port = port;
            this.database = database;
            this.username = username;
            this.password = password;
        }

        public ConnectionData(String host, String port, String database, String username, String password) {
            this.host = host;
            this.port = port;
            this.database = database;
            this.username = username;
            this.password = password;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getTableName() {
            return tableName;
        }

        public void setDatabase(String database) {
            this.database = database;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        public Connection makeConnection() {
            try {
                if (connection == null || connection.isClosed()) {
                    connection = DriverManager.getConnection("jdbc:drizzle://" + host + ":" + port + "/" + database + "?connectTimeout=3", username, password);
                }
            } catch (SQLException ex) {
                Log.e("ERR", "Could not create activeConnection");
            }
            return connection;
        }

        /**
         * Mata la conexión activa y ejecuta el runnable dado una vez matada la conexión.
         *
         * @param runAfter
         */
        public void killConnectionIfActive(Runnable runAfter) {
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                    Log.i("SQLConnectionManager", "Killed active connection.");
                    runAfter.run();
                } catch (SQLException ex) {
                    Log.e("ERR", "No se pudo matar la conexión");
                }
            }
        }

        public String getNombre() {
            return username + "@" + database;
        }

        @Override
        public String getTitulo() {
            return getNombre();
        }

        @Override
        public String getSubtitulo() {
            return host + ":" + port;
        }

        @Override
        public String getTitulo(Context context) {
            return null;
        }

        @Override
        public String getSubtitulo(Context context) {
            return null;
        }

        @Override
        public String toString() {
            return "ConnectionData{" +
                    "id=" + id +
                    ", host='" + host + '\'' +
                    ", port='" + port + '\'' +
                    ", database='" + database + '\'' +
                    ", username='" + username + '\'' +
                    ", password='" + password + "'}\n";
        }
    }

}
