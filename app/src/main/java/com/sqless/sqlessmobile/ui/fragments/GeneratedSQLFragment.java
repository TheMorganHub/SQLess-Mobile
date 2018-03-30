package com.sqless.sqlessmobile.ui.fragments;


import android.util.Log;
import android.view.View;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.busevents.createtable.MustGenerateSQLEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class GeneratedSQLFragment extends AbstractFragment {

    EventBus bus = EventBus.getDefault();

    public GeneratedSQLFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getTitle() {
        return "Crear tabla";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_generated_sql;
    }

    @Override
    public void afterCreate() {
        if (!bus.isRegistered(this)) {
            bus.register(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (bus.isRegistered(this)) {
            bus.unregister(this);
        }
    }

    @Override
    protected void implementListeners(View containerView) {
    }

    @Subscribe
    public void onMustGenerateSQLEvent(MustGenerateSQLEvent event) {
        Log.i("MSJ", "BWERK");
    }

}
