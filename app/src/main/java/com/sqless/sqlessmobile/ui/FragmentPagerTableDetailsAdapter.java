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
                f.getArguments().putString("QUERY_TITLE", connectionData != null ? connectionData.getTableName() : "");
                f.getArguments().putString("query_to_run", connectionData != null ? "SELECT * FROM " + connectionData.getTableName() + " LIMIT 200" : null);
                break;
        }
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
                return "COLUMNAS";
            case 1:
                return "CONTENIDO";
            default:
                return null;
        }
    }

}
