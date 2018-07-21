package com.sqless.sqlessmobile.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.db.queries.SQLQuery;
import com.sqless.sqlessmobile.db.queries.SQLUpdateQuery;
import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.sqlobjects.SQLTable;
import com.sqless.sqlessmobile.ui.FragmentContainer;
import com.sqless.sqlessmobile.ui.FragmentPagerCreateTableAdapter;
import com.sqless.sqlessmobile.ui.busevents.createtable.ColumnEvents;
import com.sqless.sqlessmobile.ui.busevents.createtable.MustGenerateSQLEvent;
import com.sqless.sqlessmobile.ui.fragments.AbstractFragment;
import com.sqless.sqlessmobile.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class CreateTableActivity extends AppCompatActivity implements FragmentContainer {

    private SQLTable newTable;
    EventBus bus = EventBus.getDefault();
    private SQLConnectionManager.ConnectionData connectionData;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_table);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newTable = savedInstanceState == null ? new SQLTable("") : (SQLTable) savedInstanceState.getSerializable("new_table");
        connectionData = (SQLConnectionManager.ConnectionData) (savedInstanceState == null ? getIntent().getSerializableExtra("connection_data")
                : savedInstanceState.getSerializable("connection_data"));

        bus.register(this);

        viewPager = findViewById(R.id.viewpager);
        FragmentPagerCreateTableAdapter adapter = new FragmentPagerCreateTableAdapter(this, getSupportFragmentManager(), getIntent().getExtras());
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        FloatingActionButton fab = findViewById(R.id.fab_action_create_table);
        fab.setOnClickListener(view -> adapter.getRegisteredFragment(0).onFabClicked());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                    case 1:
                        //el invokeOnUIThread le permite al adapter llenarse con los fragmentos antes de ejecutarse este bloque de código, si no esperamos, el adapter devolverá un fragment null
                        UIUtils.invokeOnUIThreadIfNotDestroyed(CreateTableActivity.this, () -> {
                            AbstractFragment fragment = adapter.getRegisteredFragment(position);
                            fab.setOnClickListener(view -> fragment.onFabClicked());
                            fab.show();
                        });
                        break;
                    case 2:
                        fab.hide();
                        bus.post(new MustGenerateSQLEvent(newTable));
                    default:
                        fab.hide();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("new_table", newTable);
        outState.putSerializable("connection_data", connectionData);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_table, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.btn_confirm_table_creation:
                confirmTableCreation();
                return true;
        }

        return false;
    }

    public void confirmTableCreation() {
        if (newTable.getColumns().isEmpty()) {
            Toast.makeText(this, "La tabla debe tener al menos una columna para poder ser creada.", Toast.LENGTH_LONG).show();
            return;
        }
        UIUtils.showInputDialog(this, "Nombre de la tabla", nombre -> {
            newTable.setName(nombre);
            SQLQuery createTableQuery = new SQLUpdateQuery(this, connectionData, newTable.generateCreateStatement()) {
                @Override
                public void onConnectionKilled() {
                    UIUtils.invokeOnUIThreadIfNotDestroyed(CreateTableActivity.this, () -> {
                        setResult(RESULT_OK, new Intent().putExtra("new_table", newTable));
                        finish();
                    });
                }

                @Override
                public void onFailure(String errMessage) {
                    UIUtils.invokeOnUIThreadIfNotDestroyed(CreateTableActivity.this, () ->
                            UIUtils.showMessageDialogWithNeutralButton(CreateTableActivity.this, "Error",
                                    "La tabla no se pudo crear. Asegúrate que su nombre, la definición de las columnas y FKs son válidos.",
                                    "Ver error SQL", () -> UIUtils.showMessageDialog(CreateTableActivity.this, "", errMessage)));
                }
            };
            createTableQuery.exec();
        }, () -> Toast.makeText(this, "El nombre de la tabla no puede estar vacío.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void getTitleFromFragment(String title) {
        setTitle(title);
    }

    @Subscribe
    public void onColumnAddedEvent(ColumnEvents.ColumnAddedEvent event) {
        newTable.addColumn(event.column);
    }

    @Subscribe
    public void onColumnRemovedEvent(ColumnEvents.ColumnRemovedEvent event) {
        newTable.removeColumn(event.column);
    }

    @Subscribe
    public void onColumnRequestEvent(ColumnEvents.ColumnRequestEvent event) {
        bus.post(new ColumnEvents.ColumnsReceivedEvent(newTable.getColumns()));
    }

    @Subscribe
    public void onFkAddedEvent(ColumnEvents.FKAddedEvent event) {
        newTable.addFK(event.foreignKey);
    }

    @Subscribe
    public void onFkRemovedEvent(ColumnEvents.FKRemovedEvent event) {
        newTable.removeFK(event.fk);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }
}
