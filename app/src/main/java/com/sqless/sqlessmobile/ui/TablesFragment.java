package com.sqless.sqlessmobile.ui;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.sqless.sqlessmobile.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TablesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TablesFragment extends AbstractFragment {

    public TablesFragment() {
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_tables;
    }

    @Override
    protected String getTitle() {
        return "Tablas";
    }

    @Override
    protected void implementListeners(View containerView) {
        containerView.findViewById(R.id.fab_create_table).setOnClickListener(view1 -> {
            Log.i("FAB", "Bwerk!");
            //TODO implement listener
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
