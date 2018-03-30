package com.sqless.sqlessmobile.ui.fragments;

import android.view.View;

import com.sqless.sqlessmobile.R;

public class CreateUnionFragment extends AbstractFragment {


    public CreateUnionFragment() {
        // Required empty public constructor
    }


    @Override
    protected String getTitle() {
        return "Create table";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_create_union;
    }

    @Override
    public void afterCreate() {

    }

    @Override
    protected void implementListeners(View containerView) {

    }
}
