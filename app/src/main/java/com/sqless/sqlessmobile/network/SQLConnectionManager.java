package com.sqless.sqlessmobile.network;

import android.app.Activity;
import android.content.Context;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.activities.MainActivity;
import com.sqless.sqlessmobile.ui.adapters.listview.Subtitulado;
import com.sqless.sqlessmobile.utils.Callback;
import com.sqless.sqlessmobile.utils.SQLUtils;
import com.sqless.sqlessmobile.utils.UIUtils;

import java.io.Serializable;
import java.nio.BufferUnderflowException;
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
     * "mysql" in MySQL using a temporary {@code Connection} object. If
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
            DriverManager.setLoginTimeout(5);
            try (Connection testCon = DriverManager.getConnection("jdbc:mysql://" + hostName + ":" + port + "/mysql", username, password)) {
                lastSuccessful = new ConnectionData(hostName, port, "mysql", username, password);

                SQLUtils.getDatabaseNames((Activity) v.getContext(), lastSuccessful, names -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_list_item_1, names);
                    ((Spinner) v.findViewById(R.id.spinner_dbs)).setAdapter(adapter);
                    v.findViewById(R.id.spinner_dbs).setVisibility(View.VISIBLE);
                    v.post(() -> callbackSuccess.exec(lastSuccessful));
                });
            } catch (SQLException | BufferUnderflowException e) {
                String exMessage = e.getMessage() != null ? e.getMessage() : "Ocurrió un error inesperado al testear la conexión. ¿Quizá la cuenta no existe?";
                Log.e("SQLConnectionManager", "Test failed: " + exMessage);
                v.post(() -> callbackFailure.exec(exMessage));
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

    public void setLastSuccessful(ConnectionData lastSuccessful) {
        this.lastSuccessful = lastSuccessful;
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
        public String testingDatabase;
        public String username;
        public String password;

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

        public void setTestingDatabase(String testingDatabase) {
            this.testingDatabase = testingDatabase;
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

        public Connection makeConnection(Activity context) {
            Connection conn = null;
            try {
                DriverManager.setLoginTimeout(5);
                conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?allowMultiQueries=true", username, password);
            } catch (SQLException e) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(context, () -> {
                    Runnable leaveActivity = () -> {
                        if (context.getClass() != MainActivity.class) {
                            context.finish();
                        }
                    };
                    UIUtils.showMessageDialog(context, "Error de conexión", "La conexión con el servidor MySQL no se pudo crear." +
                            "\nVerifica que los datos de conexión sean correctos y que el servidor se encuentre disponible para aceptar conexiones.", leaveActivity, leaveActivity);
                });
            }
            return conn;
        }

        public String getNombre() {
            return username + "@" + host + ":" + port;
        }

        @Override
        public String getTitulo() {
            return getNombre();
        }

        @Override
        public String getSubtitulo() {
            return database;
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
