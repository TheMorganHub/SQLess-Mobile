package com.sqless.sqlessmobile.ui.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.sqlobjects.SQLTable;
import com.sqless.sqlessmobile.ui.FragmentInteractionListener;
import com.sqless.sqlessmobile.ui.FragmentPagerCreateTableAdapter;
import com.sqless.sqlessmobile.ui.busevents.createtable.ColumnAddedEvent;
import com.sqless.sqlessmobile.ui.fragments.AbstractFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class CreateTableActivity extends AppCompatActivity implements FragmentInteractionListener {

    private SQLTable newTable;
    EventBus bus = EventBus.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_table);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newTable = savedInstanceState == null ? new SQLTable() : (SQLTable) savedInstanceState.getSerializable("NEW_TABLE");

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
                        AbstractFragment fragment = adapter.getRegisteredFragment(position);
                        fab.setOnClickListener(view -> fragment.onFabClicked());
                        fab.show();
                        break;
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
                //TODO create table creation script and execute. Mandar un RESULT_OK si todo salió bien.
                return true;
        }

        return false;
    }

    @Subscribe
    public void onColumnAddedEvent(ColumnAddedEvent event) {
        newTable.addColumn(event.column);
    }


    @Override
    public void onInteraction(String title, SQLConnectionManager.ConnectionData data) {
        setTitle(title);
    }

}
