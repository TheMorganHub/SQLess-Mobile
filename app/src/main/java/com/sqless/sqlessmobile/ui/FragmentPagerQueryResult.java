package com.sqless.sqlessmobile.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.ui.fragments.AbstractFragment;
import com.sqless.sqlessmobile.ui.fragments.QueryFragment;
import com.sqless.sqlessmobile.ui.fragments.TableHtmlFragment;

public class FragmentPagerQueryResult extends FragmentPagerAdapter {
    private final Bundle fragmentBundle;

    public FragmentPagerQueryResult(FragmentManager fm, Bundle data) {
        super(fm);
        this.fragmentBundle = data;
    }

    @Override
    public Fragment getItem(int position) {
        AbstractFragment f;
        SQLConnectionManager.ConnectionData connectionData = (SQLConnectionManager.ConnectionData) fragmentBundle.getSerializable("CONNECTION_DATA");
        switch (position) {
            case 0:
                f = AbstractFragment.newInstance(connectionData, TableHtmlFragment.class);
                break;
            default:
                f = AbstractFragment.newInstance(connectionData, QueryFragment.class);
                break;
        }
        f.getArguments().putString("QUERY_TITLE", fragmentBundle.getString("QUERY_TITLE"));
        f.getArguments().putString("query_to_run", fragmentBundle.getString("query_to_run"));
        return f;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "RESULTADO";
            case 1:
                return "CONSULTA";
            default:
                return null;
        }
    }
}
