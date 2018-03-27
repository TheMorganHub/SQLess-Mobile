package com.sqless.sqlessmobile.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.network.SQLConnectionManager;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ViewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private FragmentInteractionListener mListener;
    private SQLConnectionManager.ConnectionData connectionData;

    public ViewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param connectionData Parameter 1.
     * @return A new instance of fragment ViewsFragment.
     */
    public static ViewsFragment newInstance(SQLConnectionManager.ConnectionData connectionData) {
        ViewsFragment fragment = new ViewsFragment();
        Bundle args = new Bundle();
        args.putSerializable("CONNECTION_DATA", connectionData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            connectionData = (SQLConnectionManager.ConnectionData) getArguments().getSerializable("CONNECTION_DATA");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TODO implement listeners here
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_views, container, false);
    }

    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onInteraction("Vistas", connectionData);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteractionListener) {
            mListener = (FragmentInteractionListener) context;
            mListener.onInteraction("Vistas", connectionData);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
