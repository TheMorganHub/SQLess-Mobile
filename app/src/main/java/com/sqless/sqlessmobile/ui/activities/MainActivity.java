package com.sqless.sqlessmobile.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.db.HelperDB;
import com.sqless.sqlessmobile.network.GoogleTokenManager;
import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.ui.adapters.listview.ListViewDBConnectionAdapter;
import com.sqless.sqlessmobile.utils.FinalValue;
import com.sqless.sqlessmobile.utils.SQLUtils;
import com.sqless.sqlessmobile.utils.UIUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private AlertDialog activeDialog;
    private List<SQLConnectionManager.ConnectionData> connectionDataList;
    private ListViewDBConnectionAdapter<SQLConnectionManager.ConnectionData> adapter;
    private HelperDB dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Conexiones");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new HelperDB(this);
        connectionDataList = dbHelper.getConnections();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> createConnectionDialog(null));

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ListView lvConnections = findViewById(R.id.lv_connections);
        adapter = new ListViewDBConnectionAdapter<>(this, connectionDataList);

        lvConnections.setAdapter(adapter);
        lvConnections.setOnItemLongClickListener(this);
        lvConnections.setOnItemClickListener(this);
        showOrHideImageBackground();
    }

    public void loadUserInfoIntoDrawer(GoogleSignInAccount account) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        ((TextView) headerView.findViewById(R.id.tv_username)).setText(account.getDisplayName());
        ((TextView) headerView.findViewById(R.id.tv_user_email)).setText(account.getEmail());
        Glide.with(this).load(account.getPhotoUrl()).override(170, 170).into((ImageView) headerView.findViewById(R.id.iv_user_photo));
    }

    @Override
    protected void onStart() {
        super.onStart();
        signInSilently();
    }

    public void signInSilently() {
        GoogleTokenManager.getInstance().silentSignIn(this, account -> {
            Log.i("MainActivity", "Silent sign in with id token.");
            loadUserInfoIntoDrawer(account);
        });
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
            dbSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedDB = dbSpinner.getAdapter().getItem(position).toString();
                    savedConnectionData.setTestingDatabase(selectedDB);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            txtConHost.setText(savedConnectionData.host);
            txtConPort.setText(savedConnectionData.port);
            txtConUsername.setText(savedConnectionData.username);
            txtConPassword.setText(savedConnectionData.password);
            dbSpinner.setVisibility(View.VISIBLE);
            dbSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{savedConnectionData.database}));
            populateDbSpinner(savedConnectionData, viewInflated);
        }

        dialogBuilder
                .setNeutralButton("Test", null)
                .setPositiveButton("Guardar", null);
        dialogBuilder.setTitle(update ? "Editar conexión MySQL" : "Nueva conexión MySQL");
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
                Toast toast = Toast.makeText(this, "La prueba de conexión fue exitosa", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
                if (update) {
                    String testingDb = savedConnectionData.testingDatabase;
                    UIUtils.selectSpinnerItemByValue(dbSpinner, testingDb != null ? testingDb : savedConnectionData.database);
                }
            }, errorCode -> {
                positiveButtonGuardar.setEnabled(false);
                viewInflated.findViewById(R.id.spinner_dbs).setVisibility(View.INVISIBLE);
                Toast toast = Toast.makeText(this, "La prueba de conexión falló.\nVerifica tus datos de conexión.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
            });
        });
        positiveButtonGuardar.setOnClickListener(v -> {
            SQLConnectionManager.ConnectionData lastSuccessful = SQLConnectionManager.getInstance().getLastSuccessful();
            activeDialog.dismiss();
            if (lastSuccessful != null) {
                if (update) {
                    lastSuccessful.setDatabase(savedConnectionData.testingDatabase);
                    savedConnectionData.testingDatabase = null;
                    lastSuccessful.setId(savedConnectionData.getId());
                    updateConnection(lastSuccessful);
                } else {
                    lastSuccessful.setDatabase(dbSpinner.getSelectedItem().toString());
                    saveConnection(lastSuccessful);
                }
            }
        });
        positiveButtonGuardar.setEnabled(update);
    }

    public void populateDbSpinner(SQLConnectionManager.ConnectionData savedConnectionData, View dialogView) {
        SQLUtils.getDatabaseNames(this, savedConnectionData, names -> {
            Spinner spinnerDB = dialogView.findViewById(R.id.spinner_dbs);
            spinnerDB.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names));
            UIUtils.selectSpinnerItemByValue(spinnerDB, savedConnectionData.database);
            SQLConnectionManager.getInstance().setLastSuccessful(savedConnectionData);
        });
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
            UIUtils.showMessageDialog(this, "Editar conexión conexión", "No se pudo editar la conexión. Intenta de nuevo más tarde. " +
                    "Si el problema persiste, intenta reinstalando la aplicación.");
        }
    }

    public void saveConnection(SQLConnectionManager.ConnectionData connectionData) {
        long id = dbHelper.insertConnection(connectionData);
        if (id != -1) {
            connectionDataList.add(connectionData);
            connectionData.setId(id);
            adapter.notifyDataSetChanged();
        } else {
            UIUtils.showMessageDialog(this, "Guardar conexión", "No se pudo guardar la conexión. Intenta de nuevo más tarde. " +
                    "Si el problema persiste, intenta reinstalando la aplicación.");
        }
        showOrHideImageBackground();
    }

    public void showOrHideImageBackground() {
        boolean shouldDisplay = adapter.getCount() == 0;
        findViewById(R.id.image_landing_background).setVisibility(shouldDisplay ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.tv_landing_hint).setVisibility(shouldDisplay ? View.VISIBLE : View.INVISIBLE);
        findViewById(R.id.tv_landing_hint_bold).setVisibility(shouldDisplay ? View.VISIBLE : View.INVISIBLE);
    }

    public void deleteConnection(SQLConnectionManager.ConnectionData connectionData) {
        UIUtils.showConfirmationDialog(this, "Eliminar conexión", "¿Estás seguro que deseas eliminar esta conexión?", () -> {
            boolean success = dbHelper.deleteConnection(connectionData);
            if (success) {
                connectionDataList.remove(connectionData);
                adapter.notifyDataSetChanged();
            } else {
                UIUtils.showMessageDialog(this, "Eliminar conexión", "No se pudo eliminar la conexión. Intenta de nuevo más tarde. " +
                        "Si el problema persiste, intenta reinstalando la aplicación.");
            }
            showOrHideImageBackground();
        });
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
                Snackbar.make(findViewById(android.R.id.content), "Bienvenido " + account.getDisplayName() + " (" + account.getEmail() + ")", Snackbar.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_logout:
                doLogOut();
                break;
            case R.id.nav_about:
                showAbout();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void doLogOut() {
        UIUtils.showConfirmationDialog(this, "Cerrar sesión", "¿Estás seguro que deseas cerrar sesión?",
                () -> GoogleTokenManager.getInstance().logOut(this));
    }

    public void showAbout() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_about, null);
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
}
