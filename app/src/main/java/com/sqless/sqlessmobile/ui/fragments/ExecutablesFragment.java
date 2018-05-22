package com.sqless.sqlessmobile.ui.fragments;


import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.sqlobjects.SQLExecutable;
import com.sqless.sqlessmobile.sqlobjects.SQLFunction;
import com.sqless.sqlessmobile.sqlobjects.SQLProcedure;
import com.sqless.sqlessmobile.ui.adapters.listview.ListViewImageAdapter;
import com.sqless.sqlessmobile.utils.FinalValue;
import com.sqless.sqlessmobile.utils.SQLUtils;

import java.util.List;

public class ExecutablesFragment extends AbstractFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ListView lvExecutables;
    private List<SQLExecutable> executables;
    private ProgressBar progressBar;
    private int executableType;
    private ListViewImageAdapter<SQLExecutable> executablesAdapter;
    public static final int FUNCTION = 575;
    public static final int PROCEDURE = 166;
    private TextView tvNoExecutables;
    private SwipeRefreshLayout swipeRefreshLayout;

    public ExecutablesFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getTitle() {
        executableType = getArguments().getInt("EXECUTABLE_TYPE");
        return executableType == FUNCTION ? "Funciones" : "Procedures";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_executables;
    }

    @Override
    public void afterCreate() {
        lvExecutables = fragmentView.findViewById(R.id.lv_executables);
        progressBar = fragmentView.findViewById(R.id.progress_bar_executables);
        tvNoExecutables = fragmentView.findViewById(R.id.tv_no_executables_exist);
        tvNoExecutables.setText("No hay " + (executableType == FUNCTION ? "funciones" : "procedures"));

        if (executablesAdapter == null) {
            lvExecutables.setOnItemLongClickListener(this);
            lvExecutables.setOnItemClickListener(this);
            progressBar.setVisibility(View.VISIBLE);
            switch (executableType) {
                case FUNCTION:
                    SQLUtils.getExecutables(connectionData, SQLFunction.class, this::onExecutablesLoaded, err -> progressBar.setVisibility(View.GONE));
                    break;
                case PROCEDURE:
                    SQLUtils.getExecutables(connectionData, SQLProcedure.class, this::onExecutablesLoaded, err -> progressBar.setVisibility(View.GONE));
                    break;
            }
        } else {
            lvExecutables.setAdapter(executablesAdapter);
            tvNoExecutables.setVisibility(executables != null && !executables.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onRefresh() {
        switch (executableType) {
            case FUNCTION:
                SQLUtils.getExecutables(connectionData, SQLFunction.class, this::onExecutablesRefresh, err -> swipeRefreshLayout.setRefreshing(false));
                break;
            case PROCEDURE:
                SQLUtils.getExecutables(connectionData, SQLProcedure.class, this::onExecutablesRefresh, err -> swipeRefreshLayout.setRefreshing(false));
                break;
        }
    }

    public void onExecutablesLoaded(List<SQLExecutable> executables) {
        this.executables = executables;
        executablesAdapter = new ListViewImageAdapter<>(getContext(),
                getResources().getDrawable(executableType == FUNCTION ? R.drawable.ic_functions_black_24dp : R.drawable.ic_procedures_black_24dp), executables);
        lvExecutables.setAdapter(executablesAdapter);
        progressBar.setVisibility(View.GONE);
        tvNoExecutables.setVisibility(executables != null && !executables.isEmpty() ? View.GONE : View.VISIBLE);
    }

    public void onExecutablesRefresh(List<SQLExecutable> executables) {
        this.executables = executables;
        executablesAdapter = new ListViewImageAdapter<>(getContext(),
                getResources().getDrawable(executableType == FUNCTION ? R.drawable.ic_functions_black_24dp : R.drawable.ic_procedures_black_24dp), executables);
        lvExecutables.setAdapter(executablesAdapter);
        tvNoExecutables.setVisibility(executables != null && !executables.isEmpty() ? View.GONE : View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public String getFragTag() {
        return getClass().getSimpleName() + "_" + (getArguments().getInt("EXECUTABLE_TYPE") == FUNCTION ? FUNCTION : PROCEDURE);
    }

    @Override
    protected void implementListeners(View containerView) {
        swipeRefreshLayout = containerView.findViewById(R.id.lay_swipe_refresh_executables);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //TODO EXECUTE CALLABLE
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        FinalValue<AlertDialog> dialog = new FinalValue<>();
        AlertDialog.Builder actionDialog = new AlertDialog.Builder(getContext());
        actionDialog.setItems(new String[]{"Eliminar"}, (dialogInterface, clickedItem) -> {
            switch (clickedItem) {
                case 0:
                    deleteExecutable(executables.get(position));
                    break;
            }
        });
        dialog.set(actionDialog.show());
        return true;
    }

    public void deleteExecutable(SQLExecutable executable) {
        SQLUtils.dropEntity(connectionData, executable, nullobj -> {
            executables.remove(executable);
            executablesAdapter.notifyDataSetChanged();
            fragmentView.findViewById(R.id.tv_no_executables_exist).setVisibility(executables != null && !executables.isEmpty() ? View.GONE : View.VISIBLE);
        }, err -> Log.e(getClass().getSimpleName(), "No se pudo eliminar el ejecutable"));
    }
}
