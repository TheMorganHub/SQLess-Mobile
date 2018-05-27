package com.sqless.sqlessmobile.ui.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.provider.DocumentFile;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.ui.FragmentContainer;
import com.sqless.sqlessmobile.ui.FragmentPagerTableDetailsAdapter;
import com.sqless.sqlessmobile.ui.busevents.tabledata.DataEvents;
import com.sqless.sqlessmobile.ui.fragments.ExportTableFragment;
import com.sqless.sqlessmobile.utils.DataUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class TableDetailsActivity extends AppCompatActivity implements FragmentContainer {

    private ViewPager viewPager;
    EventBus bus = EventBus.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_details);
        bus.register(this);
        // Find the view pager that will allow the user to swipe between fragments
        viewPager = findViewById(R.id.viewpager);

        // Create an adapter that knows which fragment should be shown on each page
        FragmentPagerTableDetailsAdapter adapter = new FragmentPagerTableDetailsAdapter(this, getSupportFragmentManager(), getIntent().getExtras());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.btn_export_table:
                exportTableData();
                return true;
        }

        return false;
    }

    public void exportTableData() {
        ExportTableFragment exportTableFragment = ExportTableFragment.newInstance();
        Bundle bundle = new Bundle();
        bundle.putString("file_name", ((SQLConnectionManager.ConnectionData) getIntent().getSerializableExtra("CONNECTION_DATA")).getTableName());
        exportTableFragment.setArguments(bundle);
        exportTableFragment.show(getSupportFragmentManager(), "export_table_data");
    }

    @Subscribe
    public void onUriIsReadyEvent(DataEvents.URIIsReadyEvent event) {
        DocumentFile file = event.documentFile;
        if (event.eventType == DataEvents.URIIsReadyEvent.JSON_EVENT) {
            DataUtils.tableToJSON(this, (SQLConnectionManager.ConnectionData) getIntent().getSerializableExtra("CONNECTION_DATA"), file);
        } else if (event.eventType == DataEvents.URIIsReadyEvent.CSV_EVENT) {
            DataUtils.tableToCSV(this, (SQLConnectionManager.ConnectionData) getIntent().getSerializableExtra("CONNECTION_DATA"), file);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_export_table, menu);
        return true;
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
    protected void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Override
    public void getTitleFromFragment(String title) {
        setTitle(title);
    }
}
