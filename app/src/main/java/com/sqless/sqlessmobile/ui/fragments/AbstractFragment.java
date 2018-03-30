package com.sqless.sqlessmobile.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.ui.FragmentInteractionListener;

public abstract class AbstractFragment extends Fragment {
    protected FragmentInteractionListener mListener;
    protected SQLConnectionManager.ConnectionData connectionData;
    protected View fragmentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(getLayoutResId(), container, false);
        return fragmentView;
    }

    public static <T extends AbstractFragment> T newInstance(SQLConnectionManager.ConnectionData connectionData, Class<T> clazz) {
        T fragment = null;
        try {
            fragment = clazz.newInstance();
        } catch (Exception e) {

        }
        Bundle args = new Bundle();
        args.putSerializable("CONNECTION_DATA", connectionData);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * The title that the fragment will use on the context toolbar.
     *
     * @return a String that will serve as this fragment's
     */
    protected abstract String getTitle();

    /**
     * Retorna el ID del resource layout que el Fragment usará.
     *
     * @return un int de {@code R.id.layout}
     * @see #onCreateView(LayoutInflater, ViewGroup, Bundle)
     */
    protected abstract int getLayoutResId();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        afterCreate();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (getArguments() != null) {
            connectionData = (SQLConnectionManager.ConnectionData) getArguments().getSerializable("CONNECTION_DATA");
            mListener.onInteraction(getTitle(), connectionData);
        }
    }

    /**
     * Este método se ejecuta luego de {@link #onActivityCreated(Bundle)} y cuando todos los argumentos
     * pasados a este fragment ya están cargados.
     */
    public abstract void afterCreate();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteractionListener) {
            mListener = (FragmentInteractionListener) context;
            mListener.onInteraction(getTitle(), connectionData);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentInteractionListener");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        implementListeners(view.getRootView());
    }

    public void onFabClicked() {
    }

    protected abstract void implementListeners(View containerView);

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
