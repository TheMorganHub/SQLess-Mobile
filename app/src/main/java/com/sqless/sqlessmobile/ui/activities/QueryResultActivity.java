package com.sqless.sqlessmobile.ui.activities;

import android.support.design.widget.TabLayout;
import android.support.v4.provider.DocumentFile;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.ui.FragmentContainer;
import com.sqless.sqlessmobile.ui.FragmentPagerQueryResult;
import com.sqless.sqlessmobile.ui.busevents.tabledata.DataEvents;
import com.sqless.sqlessmobile.ui.fragments.ExportAsFormatSheetFragment;
import com.sqless.sqlessmobile.utils.DataUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class QueryResultActivity extends AppCompatActivity implements FragmentContainer {

    EventBus bus = EventBus.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_details);
        bus.register(this);
        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = findViewById(R.id.viewpager);

        // Create an adapter that knows which fragment should be shown on each page
        FragmentPagerQueryResult adapter = new FragmentPagerQueryResult(getSupportFragmentManager(), getIntent().getExtras());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void exportTableData() {
        ExportAsFormatSheetFragment exportAsFormat = ExportAsFormatSheetFragment.newInstance();
        exportAsFormat.show(getSupportFragmentManager(), "export_result_data");
    }

    @Subscribe
    public void onURIIsReadyEvent(DataEvents.URIIsReadyEvent event) {
        if (!event.forActivity.equals(getClass().getName())) {
            return;
        }
        DocumentFile file = event.documentFile;
        String query = getIntent().getStringExtra("query_to_export");
        String resultName = getIntent().getStringExtra("result_name");
        SQLConnectionManager.ConnectionData connectionData = (SQLConnectionManager.ConnectionData) getIntent().getSerializableExtra("connection_data");
        if (event.eventType == DataEvents.URIIsReadyEvent.JSON_EVENT) {
            DataUtils.resultToJSON(this, connectionData, query, resultName, file);
        } else if (event.eventType == DataEvents.URIIsReadyEvent.CSV_EVENT) {
            DataUtils.resultToCSV(this, connectionData, query, resultName, file);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_export_table, menu);
        return true;
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
