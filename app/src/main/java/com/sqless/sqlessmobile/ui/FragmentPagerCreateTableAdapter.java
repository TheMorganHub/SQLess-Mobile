package com.sqless.sqlessmobile.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.ui.fragments.AbstractFragment;
import com.sqless.sqlessmobile.ui.fragments.CreateColumnsFragment;
import com.sqless.sqlessmobile.ui.fragments.CreateUnionFragment;
import com.sqless.sqlessmobile.ui.fragments.GeneratedSQLFragment;

public class FragmentPagerCreateTableAdapter extends FragmentPagerAdapter {
    private Context mContext;
    private final Bundle fragmentBundle;
    SparseArray<AbstractFragment> registeredFragments = new SparseArray<>();


    public FragmentPagerCreateTableAdapter(Context context, FragmentManager fm, Bundle data) {
        super(fm);
        mContext = context;
        this.fragmentBundle = data;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        AbstractFragment fragment = (AbstractFragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public AbstractFragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        AbstractFragment f;
        SQLConnectionManager.ConnectionData connectionData = (SQLConnectionManager.ConnectionData) fragmentBundle.getSerializable("CONNECTION_DATA");
        switch (position) {
            case 0:
                f = AbstractFragment.newInstance(connectionData, CreateColumnsFragment.class);
                break;
            case 1:
                f = AbstractFragment.newInstance(connectionData, CreateUnionFragment.class);
                break;
            default:
                f = AbstractFragment.newInstance(connectionData, GeneratedSQLFragment.class);
                break;
        }
        return f;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return "COLUMNAS";
            case 1:
                return "FKS";
            case 2:
                return "SQL";
            default:
                return null;
        }
    }
}
