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
import com.sqless.sqlessmobile.ui.fragments.MapleCrearFragment;
import com.sqless.sqlessmobile.ui.fragments.MapleResultadoFragment;

public class FragmentPagerMapleAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private final Bundle fragmentBundle;
    SparseArray<AbstractFragment> registeredFragments = new SparseArray<>();

    public FragmentPagerMapleAdapter(Context context, FragmentManager fm, Bundle fragmentBundle) {
        super(fm);
        this.fragmentBundle = fragmentBundle;
        mContext = context;
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
        SQLConnectionManager.ConnectionData connectionData = (SQLConnectionManager.ConnectionData) fragmentBundle.getSerializable("connection_data");
        switch (position) {
            case 0:
                f = AbstractFragment.newInstance(connectionData, MapleCrearFragment.class);
                break;
            default:
                f = AbstractFragment.newInstance(connectionData, MapleResultadoFragment.class);
                break;
        }
        return f;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return "CREAR";
            case 1:
                return "RESULTADO";
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
