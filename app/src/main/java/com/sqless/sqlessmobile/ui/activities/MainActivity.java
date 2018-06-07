package com.sqless.sqlessmobile.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.db.HelperDB;
import com.sqless.sqlessmobile.network.GoogleTokenManager;
import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.ui.adapters.listview.ListViewDBConnectionAdapter;
import com.sqless.sqlessmobile.utils.FinalValue;
import com.sqless.sqlessmobile.utils.UIUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private AlertDialog activeDialog;
    private List<SQLConnectionManager.ConnectionData> connectionDataList;
    private ListViewDBConnectionAdapter<SQLConnectionManager.ConnectionData> adapter;
    private HelperDB dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new HelperDB(this);
        connectionDataList = dbHelper.getConnections();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> createConnectionDialog(null));
        ListView lvConnections = findViewById(R.id.lv_connections);
        adapter = new ListViewDBConnectionAdapter<>(this, connectionDataList);

        lvConnections.setAdapter(adapter);
        lvConnections.setOnItemLongClickListener(this);
        lvConnections.setOnItemClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        signInSilently();
    }

    public void signInSilently() {
        GoogleTokenManager.getInstance().silentSignIn(this, account -> Log.i("MainActivity", "Silent sign in with id token."));
    }

    public void createConnectionDialog(SQLConnectionManager.ConnectionData savedConnectionData) {
        boolean update = savedConnectionData != null;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        View viewInflated = LayoutInflater.from(MainActivity.this).inflate(R.layout.new_connection_dialog, findViewById(android.R.id.content), false);
        EditText txtConHost = viewInflated.findViewById(R.id.txt_con_host);
        EditText txtConPort = viewInflated.findViewById(R.id.txt_con_port);
        EditText txtConUsername = viewInflated.findViewById(R.id.txt_con_username);
        EditText txtConPassword = viewInflated.findViewById(R.id.txt_con_password);
        Spinner dbSpinner = viewInflated.findViewById(R.id.spinner_dbs);
        if (update) {
            txtConHost.setText(savedConnectionData.host);
            txtConPort.setText(savedConnectionData.port);
            txtConUsername.setText(savedConnectionData.username);
            txtConPassword.setText(savedConnectionData.password);
            dbSpinner.setVisibility(View.VISIBLE);
            dbSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{savedConnectionData.database}));
        }

        dialogBuilder
                .setNeutralButton("Test", null)
                .setPositiveButton("Guardar", null);
        dialogBuilder.setTitle(update ? "Editar conexión" : "Nueva conexión");
        dialogBuilder.setView(viewInflated);
        activeDialog = dialogBuilder.show();

        Button neutralButtonTest = activeDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        Button positiveButtonGuardar = activeDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        neutralButtonTest.setOnClickListener(v -> {
            String host = txtConHost.getText().toString();
            String port = txtConPort.getText().toString();
            String username = txtConUsername.getText().toString();
            String password = txtConPassword.getText().toString();
            SQLConnectionManager.getInstance().testConnection(viewInflated, username, password, host, port, connectionData -> {
                activeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                Toast.makeText(this, "La prueba de conexión fue exitosa", Toast.LENGTH_SHORT).show();
                if (update) {
                    UIUtils.selectSpinnerItemByValue(dbSpinner, savedConnectionData.database);
                }
            }, errorCode -> {
                positiveButtonGuardar.setEnabled(false);
                viewInflated.findViewById(R.id.spinner_dbs).setVisibility(View.INVISIBLE);
                Toast.makeText(this, "La prueba de conexión falló", Toast.LENGTH_SHORT).show();
            });
        });
        positiveButtonGuardar.setOnClickListener(v -> {
            SQLConnectionManager.ConnectionData lastSuccessful = SQLConnectionManager.getInstance().getLastSuccessful();
            lastSuccessful.setDatabase(dbSpinner.getSelectedItem().toString());
            activeDialog.dismiss();
            if (update) {
                lastSuccessful.setId(savedConnectionData.getId());
                updateConnection(lastSuccessful);
            } else {
                saveConnection(lastSuccessful);
            }
        });
        positiveButtonGuardar.setEnabled(false);
    }

    public void updateConnection(SQLConnectionManager.ConnectionData connectionData) {
        boolean success = dbHelper.updateConnection(connectionData);
        if (success) {
            for (int i = 0; i < connectionDataList.size(); i++) {
                if (connectionData.getId() == connectionDataList.get(i).getId()) {
                    connectionDataList.set(i, connectionData);
                    break;
                }
            }
            adapter.notifyDataSetChanged();
        } else {
            Log.e("ERR", "UPDATE");
        }
    }

    public void saveConnection(SQLConnectionManager.ConnectionData connectionData) {
        long id = dbHelper.insertConnection(connectionData);
        if (id != -1) {
            connectionDataList.add(connectionData);
            connectionData.setId(id);
            adapter.notifyDataSetChanged();
        } else {
            Log.e("ERR", "INSERT");
        }
    }

    public void deleteConnection(SQLConnectionManager.ConnectionData connectionData) {
        boolean success = dbHelper.deleteConnection(connectionData);
        if (success) {
            connectionDataList.remove(connectionData);
            adapter.notifyDataSetChanged();
        } else {
            Log.e("ERR", "DELETE");
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        FinalValue<AlertDialog> dialog = new FinalValue<>();
        SQLConnectionManager.ConnectionData selectedItem = adapter.getItem(i);
        AlertDialog.Builder actionDialog = new AlertDialog.Builder(this);
        actionDialog.setItems(R.array.action_connections, (dialogInterface, clickedItem) -> {
            switch (clickedItem) {
                case 0:
                    deleteConnection(selectedItem);
                    break;
                case 1:
                    dialog.getValue().dismiss();
                    createConnectionDialog(selectedItem);
                    break;
            }
        });
        dialog.set(actionDialog.show());
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        SQLConnectionManager.ConnectionData selectedItem = adapter.getItem(i);
        Intent intent = new Intent(this, DatabaseActionsActivity.class);
        intent.putExtra("connection_data", selectedItem);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GoogleTokenManager.ACC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                GoogleSignInAccount account = data.getParcelableExtra("ACCOUNT");
                Toast.makeText(this, "Bienvenido " + account.getEmail(), Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activeDialog != null) {
            activeDialog.dismiss();
            activeDialog = null;
        }
    }
}