package com.sqless.sqlessmobile.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.db.queries.SQLQuery;
import com.sqless.sqlessmobile.db.queries.SQLUpdateQuery;
import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.sqlobjects.SQLTable;
import com.sqless.sqlessmobile.ui.FragmentInteractionListener;
import com.sqless.sqlessmobile.ui.FragmentPagerCreateTableAdapter;
import com.sqless.sqlessmobile.ui.busevents.createtable.ColumnEvents;
import com.sqless.sqlessmobile.ui.busevents.createtable.MustGenerateSQLEvent;
import com.sqless.sqlessmobile.ui.fragments.AbstractFragment;
import com.sqless.sqlessmobile.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class CreateTableActivity extends AppCompatActivity implements FragmentInteractionListener {

    private SQLTable newTable;
    EventBus bus = EventBus.getDefault();
    private SQLConnectionManager.ConnectionData connectionData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_table);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newTable = savedInstanceState == null ? new SQLTable("") : (SQLTable) savedInstanceState.getSerializable("NEW_TABLE");
        connectionData = (SQLConnectionManager.ConnectionData) (savedInstanceState == null ? getIntent().getSerializableExtra("CONNECTION_DATA")
                : savedInstanceState.getSerializable("CONNECTION_DATA"));

        bus.register(this);

        ViewPager viewPager = findViewById(R.id.viewpager);
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
                        UIUtils.invokeOnUIThread(() -> {
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
        outState.putSerializable("NEW_TABLE", newTable);
        outState.putSerializable("CONNECTION_DATA", connectionData);
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
        UIUtils.showInputDialog(this, "Nombre", nombre -> {
            newTable.setName(nombre);
            SQLQuery createTableQuery = new SQLUpdateQuery(connectionData, newTable.generateCreateStatement()) {
                @Override
                public void onConnectionKilled() {
                    UIUtils.invokeOnUIThread(() -> {
                        setResult(RESULT_OK, new Intent().putExtra("NEW_TABLE", newTable));
                        finish();
                    });
                }

                @Override
                public void onFailure(String errMessage) {
                    Log.e("ERR", errMessage);
                    UIUtils.invokeOnUIThread(() -> Toast.makeText(CreateTableActivity.this, "Hubo un error al crear la tabla", Toast.LENGTH_SHORT).show());
                }
            };
            createTableQuery.exec();
        });
    }

    @Override
    public void onInteraction(String title, SQLConnectionManager.ConnectionData data) {
        setTitle(title);
    }

    @Subscribe
    public void onColumnAddedEvent(ColumnEvents.ColumnAddedEvent event) {
        newTable.addColumn(event.column);
    }

    @Subscribe
    public void onColumnRequestEvent(ColumnEvents.ColumnRequestEvent event) {
        bus.post(new ColumnEvents.ColumnsReceivedEvent(newTable.getColumns()));
    }

    @Subscribe
    public void onFkAddedEvent(ColumnEvents.FKAddedEvent event) {
        newTable.addFK(event.foreignKey);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }
}
