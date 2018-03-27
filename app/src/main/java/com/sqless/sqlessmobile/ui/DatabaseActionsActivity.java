package com.sqless.sqlessmobile.ui;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.utils.UIUtils;

public class DatabaseActionsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FragmentInteractionListener {

    private SQLConnectionManager.ConnectionData connectionData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_actions);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        connectionData = (SQLConnectionManager.ConnectionData) getIntent().getSerializableExtra("CONNECTION_DATA");

        View headerView = navigationView.getHeaderView(0);
        ((TextView) headerView.findViewById(R.id.nav_database_name)).setText(connectionData.database);
        ((TextView) headerView.findViewById(R.id.nav_host_name)).setText(connectionData.host);

        if (savedInstanceState == null) {
            Fragment defaultFragment = getSupportFragmentManager().findFragmentByTag("TablesFragment");
            if (defaultFragment == null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment tablesFragment = AbstractFragment.newInstance(connectionData, TablesFragment.class);
                fragmentManager.beginTransaction().replace(R.id.content_database_actions, tablesFragment, UIUtils.getTagForFragment(tablesFragment)).commit();
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.database_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;

        int id = item.getItemId();

        if (id == R.id.nav_tables_fragment) {
            fragment = TablesFragment.newInstance(connectionData, TablesFragment.class);
        } else if (id == R.id.nav_views_fragment) {
            fragment = ViewsFragment.newInstance(connectionData);
        } else if (id == R.id.nav_functions_fragment) {

        } else if (id == R.id.nav_procedures_fragment) {

        }
        openFragment(fragment);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openFragment(Fragment newFragment) {
        if (newFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragByTag = fragmentManager.findFragmentByTag(UIUtils.getTagForFragment(newFragment));
            if (fragByTag == null) { //el fragment no existe
                fragmentManager.beginTransaction().replace(R.id.content_database_actions, newFragment, UIUtils.getTagForFragment(newFragment)).commit();
            }
        }
    }

    @Override
    public void onInteraction(String title, SQLConnectionManager.ConnectionData data) {
        setTitle(title);
    }
}
