package com.sqless.sqlessmobile.ui;


import android.support.v4.app.Fragment;
import android.view.View;

import com.sqless.sqlessmobile.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class IndicesFragment extends AbstractFragment {


    public IndicesFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getTitle() {
        return connectionData != null ? connectionData.getTableName() : "";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_indices;
    }

    @Override
    public void afterCreate() {

    }

    @Override
    protected void implementListeners(View containerView) {

    }
}
