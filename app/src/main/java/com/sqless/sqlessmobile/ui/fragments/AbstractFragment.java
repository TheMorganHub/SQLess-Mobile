package com.sqless.sqlessmobile.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.ui.FragmentContainer;

public abstract class AbstractFragment extends Fragment {
    protected FragmentContainer mListener;
    protected SQLConnectionManager.ConnectionData connectionData;
    protected View fragmentView;
    protected AlertDialog activeDialog;
    private Class<? extends AbstractFragment> fragmentClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentView = inflater.inflate(getLayoutResId(), container, false);
        return fragmentView;
    }

    public static AbstractFragment newInstance(SQLConnectionManager.ConnectionData connectionData, Class<? extends AbstractFragment> clazz) {
        AbstractFragment fragment = null;
        try {
            fragment = clazz.newInstance();
            fragment.setFragmentClass(clazz);
            Bundle args = new Bundle();
            args.putSerializable("CONNECTION_DATA", connectionData);
            fragment.setArguments(args);
        } catch (Exception e) {
            Log.e(AbstractFragment.class.getSimpleName(), "Could not create fragment. Error: " + e.getMessage());
        }
        return fragment;
    }

    private void setFragmentClass(Class<? extends AbstractFragment> fragmentClass) {
        this.fragmentClass = fragmentClass;
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
            mListener.getTitleFromFragment(getTitle());
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
        if (context instanceof FragmentContainer) {
            mListener = (FragmentContainer) context;
            mListener.getTitleFromFragment(getTitle());
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentContainer");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        implementListeners(view.getRootView());
    }

    public void onFabClicked() {
    }

    /**
     * Retorna un tag que actuará como identificador de este fragment.
     * Cualquier implementación de este método debe ser determinístico, es decir,
     * el tag generado por una implementación debe ser el mismo sin importar las veces que se
     * ejecute el método.
     *
     * @return un tag que identificará al fragment.
     */
    public String getFragTag() {
        return fragmentClass.getSimpleName();
    }

    protected abstract void implementListeners(View containerView);

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (activeDialog != null) {
            activeDialog.dismiss();
            activeDialog = null;
        }
    }

    @Override
    public String toString() {
        return getTag();
    }
}
