package com.sqless.sqlessmobile.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.sqlobjects.SQLColumn;
import com.sqless.sqlessmobile.sqlobjects.SQLSelectable;
import com.sqless.sqlessmobile.ui.activities.QueryResultActivity;
import com.sqless.sqlessmobile.ui.adapters.listview.ListViewImageAdapter;
import com.sqless.sqlessmobile.utils.Callback;
import com.sqless.sqlessmobile.utils.FinalValue;
import com.sqless.sqlessmobile.utils.SQLUtils;

import java.util.List;

/**
 * Un fragment que se encarga de mostrar columnas de vistas y tablas. Uno de los argumentos que
 * llevará este fragment es el de {@code TABLE_TYPE}. Si {@code TABLE_TYPE} es {@code TABLE},
 * la clase cargará las columnas llamando al método {@link SQLUtils#getColumns(Activity, SQLConnectionManager.ConnectionData, Callback, Callback)} .
 * De lo contrario si {@code TABLE_TYPE} es {@code VIEW}, la clase cargará las columnas llamando
 * a {@link SQLUtils#getViewColumns(Activity, SQLConnectionManager.ConnectionData, Callback, Callback)} .
 * Esta diferencia es necesaria ya que en el caso de columnas de tablas, es necesario traer PK y FK.
 * En Views solo necesitamos los nombres.
 */
public class ColumnsFragment extends AbstractFragment implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ListViewImageAdapter<SQLColumn> columnsAdapter;
    private List<SQLColumn> columns;
    private ListView lv_columnas;
    private ProgressBar progressBar;
    public static final int TABLE = 632;
    public static final int VIEW = 865;
    private SwipeRefreshLayout swipeRefreshLayout;

    public ColumnsFragment() {
        // Required empty public constructor
    }

    @Override
    public void afterCreate() {
        final int tableType = getArguments().getInt("TABLE_TYPE", -1);
        progressBar = fragmentView.findViewById(R.id.column_progress_bar);
        if (columnsAdapter == null) { //el fragment está siendo cargado por primera vez
            progressBar.setVisibility(View.VISIBLE);
            switch (tableType) {
                case TABLE:
                    SQLUtils.getColumns(getActivity(), connectionData, this::onColumnsLoaded, err -> progressBar.setVisibility(View.GONE));
                    break;
                case VIEW:
                    SQLUtils.getViewColumns(getActivity(), connectionData, this::onColumnsLoaded, err -> progressBar.setVisibility(View.GONE));
                    break;
            }
        } else { //ya existe una instancia del fragment
            lv_columnas.setAdapter(columnsAdapter);
        }
    }

    @Override
    public void onRefresh() {
        final int tableType = getArguments().getInt("TABLE_TYPE", -1);
        switch (tableType) {
            case TABLE:
                SQLUtils.getColumns(getActivity(), connectionData, this::onColumnsRefresh, err -> swipeRefreshLayout.setRefreshing(false));
                break;
            case VIEW:
                SQLUtils.getViewColumns(getActivity(), connectionData, this::onColumnsRefresh, err -> swipeRefreshLayout.setRefreshing(false));
                break;
        }
    }

    public void onColumnsRefresh(List<SQLColumn> columns) {
        this.columns = columns;
        columnsAdapter = new ListViewImageAdapter<>(getContext(), columns);
        lv_columnas.setAdapter(columnsAdapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    public void onColumnsLoaded(List<SQLColumn> columns) {
        this.columns = columns;
        columnsAdapter = new ListViewImageAdapter<>(getContext(), columns);
        lv_columnas.setAdapter(columnsAdapter);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void implementListeners(View containerView) {
        swipeRefreshLayout = containerView.findViewById(R.id.lay_swipe_refresh_columns);
        swipeRefreshLayout.setOnRefreshListener(this);
        lv_columnas = containerView.findViewById(R.id.lv_columnas);
        lv_columnas.setOnItemLongClickListener(this);
        lv_columnas.setOnItemClickListener(this);
    }

    @Override
    protected String getTitle() {
        return connectionData != null ? connectionData.getTableName() : "";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_columns;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (getArguments().getInt("TABLE_TYPE", -1) != VIEW) {
            FinalValue<AlertDialog> dialog = new FinalValue<>();
            AlertDialog.Builder actionDialog = new AlertDialog.Builder(getContext());
            actionDialog.setItems(new String[]{"Eliminar"}, (dialogInterface, clickedItem) -> {
                switch (clickedItem) {
                    case 0:
                        deleteColumn(columns.get(position));
                        break;
                }
            });
            dialog.set(actionDialog.show());
            return true;
        }
        return false;
    }

    public void deleteColumn(SQLColumn column) {
        SQLUtils.dropEntity(getActivity(), connectionData, column, () -> {
            columns.remove(column);
            columnsAdapter.notifyDataSetChanged();
            fragmentView.findViewById(R.id.tv_no_columns_exist).setVisibility(columns != null && !columns.isEmpty() ? View.GONE : View.VISIBLE);
        }, err -> Log.e(getClass().getSimpleName(), "Hubo un error al eliminar columna"));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SQLColumn column = columns.get(position);
        Intent intent = new Intent(getContext(), QueryResultActivity.class);
        intent.putExtra("CONNECTION_DATA", connectionData);
        intent.putExtra("QUERY_TITLE", column.getParentName() + "." + column.getName());
        intent.putExtra("query_to_run", column.getSelectStatement(200));
        intent.putExtra("query_to_export", column.getSelectStatement(SQLSelectable.ALL));
        intent.putExtra("result_name", column.getParentName() + "_" + column.getName());
        startActivity(intent);
    }
}
