package com.sqless.sqlessmobile.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sqless.sqlessmobile.network.SQLConnectionManager;

public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {
    private Context mContext;
    private final Bundle fragmentBundle;

    public SimpleFragmentPagerAdapter(Context context, FragmentManager fm, Bundle data) {
        super(fm);
        mContext = context;
        this.fragmentBundle = data;
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        Fragment f;
        SQLConnectionManager.ConnectionData connectionData = (SQLConnectionManager.ConnectionData) fragmentBundle.getSerializable("CONNECTION_DATA");
        switch (position) {
            case 0:
                f = AbstractFragment.newInstance(connectionData, ColumnsFragment.class);
                break;
            default:
                f = AbstractFragment.newInstance(connectionData, IndicesFragment.class);
                break;
        }
        return f;
    }

    // This determines the number of tabs
    @Override
    public int getCount() {
        return 2;
    }

    // This determines the title for each tab
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return "COLUMNAS";
            case 1:
                return "INDICES";
            default:
                return null;
        }
    }

}
