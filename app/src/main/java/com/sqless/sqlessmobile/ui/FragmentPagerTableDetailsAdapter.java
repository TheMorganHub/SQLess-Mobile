package com.sqless.sqlessmobile.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.ui.fragments.AbstractFragment;
import com.sqless.sqlessmobile.ui.fragments.ColumnsFragment;
import com.sqless.sqlessmobile.ui.fragments.TableHtmlFragment;

public class FragmentPagerTableDetailsAdapter extends FragmentPagerAdapter {
    private Context mContext;
    private final Bundle fragmentBundle;
    private int tableType;

    public FragmentPagerTableDetailsAdapter(Context context, FragmentManager fm, Bundle data) {
        super(fm);
        mContext = context;
        this.fragmentBundle = data;
        tableType = data.getInt("TABLE_TYPE");
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        Fragment f;
        SQLConnectionManager.ConnectionData connectionData = (SQLConnectionManager.ConnectionData) fragmentBundle.getSerializable("CONNECTION_DATA");
        switch (position) {
            case 0:
                f = AbstractFragment.newInstance(connectionData, ColumnsFragment.class);
                f.getArguments().putInt("TABLE_TYPE", tableType);
                break;
            default:
                f = AbstractFragment.newInstance(connectionData, TableHtmlFragment.class);
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
                return "CONTENIDO";
            default:
                return null;
        }
    }

}
