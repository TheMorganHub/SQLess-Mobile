package com.sqless.sqlessmobile.ui.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.WindowManager;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.FragmentContainer;
import com.sqless.sqlessmobile.ui.FragmentPagerMapleAdapter;
import com.sqless.sqlessmobile.ui.fragments.AbstractFragment;
import com.sqless.sqlessmobile.utils.UIUtils;

public class MapleActivity extends AppCompatActivity implements FragmentContainer {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maple);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        FloatingActionButton fab = findViewById(R.id.fab_run_maple);
        ViewPager viewPager = findViewById(R.id.viewpager);
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
                        UIUtils.invokeOnUIThread(() -> {
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
