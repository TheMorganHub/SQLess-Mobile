package com.sqless.sqlessmobile.network;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.adapters.Subtitulado;
import com.sqless.sqlessmobile.utils.Callback;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class manages everything related to SQLess's connection with the SQLDB
 * engine. Whenever a connection is made, this class' {@code SQLDatabase} object
 * will hold the database to which SQLess is currently connected.
 * <p>
 * A lot of the methods in this class are mirrored by {@code SQLUtils} for the
 * sake of convenience, always referencing this class.</p>
 * <p>
 * This class follows the singleton pattern, because there will only be one set
 * of drivers loaded and one connection to the engine.</p>
 *
 * @author David Orquin, Tomás Casir, Valeria Fornieles
 */
public class SQLConnectionManager {

    private static SQLConnectionManager INSTANCE;
    private Connection masterConnection;
    private Connection connection;
    private String username;
    private String password;
    private String hostName;
    private String port;
    private String serverHostName;
    private ConnectionData lastSuccessful;

    private SQLConnectionManager() {
    }

    public Connection newQueryConnection() {
        Connection newCon = null;
        try {
            DriverManager.setLoginTimeout(3);
            newCon = DriverManager.getConnection("jdbc:mysql://" + hostName + ":" + port + "/sqless"
                    + "?zeroDateTimeBehavior=convertToNull&allowMultiQueries=true", username, password);
        } catch (SQLException e) {
        }
        return newCon;
    }

//    private boolean connectToDatabase(String dbName, String username, String password,
//                                      String hostName, String port) {
//        try {
//            long start = System.currentTimeMillis();
//
//            DriverManager.setLoginTimeout(3);
//            connection = DriverManager.getConnection("jdbc:mysql://" + hostName + ":" + port + "/" + dbName
//                    + "?zeroDateTimeBehavior=convertToNull", username, password);
//
//            long elapsed = System.currentTimeMillis() - start;
//            this.hostName = hostName;
//            this.username = username;
//            this.password = password;
//            this.port = port;
//            Log.i("SQLConnectionManager", "[ConnectionManager]: Connected to " + dbName + " at " + hostName + ":" + port
//                    + " as " + username + " in " + elapsed + "ms");
//            return true;
//        } catch (SQLException e) {
//            System.err.println(e.getMessage());
//        }
//        return false;
//    }

    /**
     * Tests a specified connection by attempting to connect to database
     * "master" in SQLServer using a temporary {@code Connection} object. If
     * successful, the {@code Connection} is then promptly closed.
     *
     * @param username
     * @param password
     * @param hostName
     * @param port
     * @param callbackSuccess
     */
    public void testConnection(View v, String username, String password, String hostName, String port, Callback<ConnectionData> callbackSuccess, Callback<String> callbackFailure) {
        final ConstraintLayout layProgress = v.findViewById(R.id.lay_progress_bar);
        final ConstraintLayout layInner = v.findViewById(R.id.lay_inner);
        if (v != null) {
            if (layProgress != null && layInner != null) {
                layInner.setVisibility(View.INVISIBLE);
                layProgress.setVisibility(View.VISIBLE);
            }
        }
        Thread conTestThread = new Thread(() -> {
            try (Connection testCon = DriverManager.getConnection("jdbc:drizzle://" + hostName + ":" + port + "/mysql?connectTimeout=3", username, password)) {
                Log.i("SQLConnectionManager", "Testing connection at " + hostName + ":" + port + " as " + username);
                Log.i("SQLConnectionManager", "Test successful");
                lastSuccessful = new ConnectionData(hostName, port, username, password);
                if (masterConnection == null) {
                    masterConnection = DriverManager.getConnection("jdbc:drizzle://" + hostName + ":" + port + "/mysql?connectTimeout=3", username, password);
                }
                v.post(() -> {
                    callbackSuccess.exec(lastSuccessful);
                });
            } catch (SQLException e) {
                Log.e("SQLConnectionManager", "Test failed");
                Log.e("SQLConnectionManager", e.getMessage());
                masterConnection = null;
                v.post(() -> {
                    callbackFailure.exec(e.getMessage());
                });
            } finally {
                if (v != null) {
                    v.post(() -> {
                        if (layProgress != null && layInner != null) {
                            layProgress.setVisibility(View.INVISIBLE);
                            layInner.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
        conTestThread.start();
    }

    public ConnectionData getLastSuccessful() {
        return lastSuccessful;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getClientHostname() {
        String hostname = "Unknown";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException ex) {
            System.out.println("Hostname can not be resolved");
        }
        return hostname;
    }

    public void closeMasterConnection() {
        if (masterConnection != null) {
            try {
                masterConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getMasterConnection() {
        return masterConnection;
    }

    public static SQLConnectionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SQLConnectionManager();
        }
        return INSTANCE;
    }

    public static class ConnectionData implements Subtitulado {
        public String host;
        public String port;
        public String username;
        public String password;

        public ConnectionData(String host, String port, String username, String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }

//        public Connection makeConnection() {
//            Connection conn = null;
//            try {
//                conn = DriverManager.getConnection("jdbc:drizzle://" + host + ":" + port + "/mysql?connectTimeout=3", username, password);
//            } catch (SQLException ex) {
//                Log.e("ERR", "Could not create connection");
//            }
//            return conn;
//        }

        @Override
        public String getTitulo() {
            return username;
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
    }

}
