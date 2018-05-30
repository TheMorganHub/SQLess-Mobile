package com.sqless.sqlessmobile.ui.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.FragmentContainer;
import com.sqless.sqlessmobile.ui.FragmentPagerMapleAdapter;
import com.sqless.sqlessmobile.ui.busevents.maplequery.MapleExecutionReadyEvent;
import com.sqless.sqlessmobile.ui.fragments.AbstractFragment;
import com.sqless.sqlessmobile.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MapleActivity extends AppCompatActivity implements FragmentContainer {

    EventBus bus = EventBus.getDefault();
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maple);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (!bus.isRegistered(this)) {
            bus.register(this);
        }

        FloatingActionButton fab = findViewById(R.id.fab_run_maple);
        viewPager = findViewById(R.id.viewpager);
        FragmentPagerMapleAdapter adapter = new FragmentPagerMapleAdapter(this, getSupportFragmentManager(), getIntent().getExtras());
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        fab.setOnClickListener(view -> adapter.getRegisteredFragment(0).onFabClicked());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        //el invokeOnUIThread le permite al adapter llenarse con los fragmentos antes de ejecutarse este bloque de código, si no esperamos, el adapter devolverá un fragment null
                        UIUtils.invokeOnUIThreadIfNotDestroyed(MapleActivity.this, () -> {
                            AbstractFragment fragment = adapter.getRegisteredFragment(position);
                            fab.setOnClickListener(view -> fragment.onFabClicked());
                            fab.show();
                        });
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

    @Subscribe
    public void onMapleExecutionReadyEvent(MapleExecutionReadyEvent event) {
        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setCurrentItem(1, true);
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
    protected void onDestroy() {
        super.onDestroy();
        if (bus.isRegistered(this)) {
            bus.unregister(this);
        }
    }

    @Override
    public void getTitleFromFragment(String title) {
        setTitle(title);
    }
}
