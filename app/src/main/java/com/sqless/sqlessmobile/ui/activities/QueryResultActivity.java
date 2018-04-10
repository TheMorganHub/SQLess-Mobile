package com.sqless.sqlessmobile.ui.activities;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.FragmentContainer;
import com.sqless.sqlessmobile.ui.FragmentPagerQueryResult;

public class QueryResultActivity extends AppCompatActivity implements FragmentContainer {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_details);
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
    public void getTitleFromFragment(String title) {
        setTitle(title);
    }
}
