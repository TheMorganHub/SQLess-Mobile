package com.sqless.sqlessmobile.ui.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sqless.sqlessmobile.R;

import org.greenrobot.eventbus.EventBus;

public class GeneratedSQLFragment extends AbstractFragment {

    EventBus bus = EventBus.getDefault();

    public GeneratedSQLFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_generated_sql, container, false);
    }

    @Override
    protected String getTitle() {
        return "Create table";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_generated_sql;
    }

    @Override
    public void afterCreate() {

    }

    @Override
    protected void implementListeners(View containerView) {
    }

}
