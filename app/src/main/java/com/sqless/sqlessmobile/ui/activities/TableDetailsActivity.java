package com.sqless.sqlessmobile.ui.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.FragmentContainer;
import com.sqless.sqlessmobile.ui.FragmentPagerTableDetailsAdapter;

public class TableDetailsActivity extends AppCompatActivity implements FragmentContainer {

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_details);
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
        }

        return false;
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
    public void getTitleFromFragment(String title) {
        setTitle(title);
    }
}
