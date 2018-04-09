package com.sqless.sqlessmobile.ui.fragments;

import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.sqlobjects.SQLColumn;
import com.sqless.sqlessmobile.ui.adapters.listview.ListViewImageAdapter;
import com.sqless.sqlessmobile.utils.Callback;
import com.sqless.sqlessmobile.utils.FinalValue;
import com.sqless.sqlessmobile.utils.SQLUtils;

import java.util.List;

/**
 * Un fragment que se encarga de mostrar columnas de vistas y tablas. Uno de los argumentos que
 * llevará este fragment es el de {@code TABLE_TYPE}. Si {@code TABLE_TYPE} es {@code TABLE},
 * la clase cargará las columnas llamando al método {@link SQLUtils#getColumns(SQLConnectionManager.ConnectionData, Callback, Callback)}.
 * De lo contrario si {@code TABLE_TYPE} es {@code VIEW}, la clase cargará las columnas llamando
 * a {@link SQLUtils#getViewColumns(SQLConnectionManager.ConnectionData, Callback, Callback)}.
 * Esta diferencia es necesaria ya que en el caso de columnas de tablas, es necesario traer PK y FK.
 * En Views solo necesitamos los nombres.
 */
public class ColumnsFragment extends AbstractFragment implements AdapterView.OnItemLongClickListener {

    private ListViewImageAdapter<SQLColumn> columnsAdapter;
    private List<SQLColumn> columns;
    private ListView lv_columnas;
    private ProgressBar progressBar;
    public static final int TABLE = 632;
    public static final int VIEW = 865;

    public ColumnsFragment() {
        // Required empty public constructor
    }

    @Override
    public void afterCreate() {
        final int tableType = getArguments().getInt("TABLE_TYPE", -1);

        lv_columnas = fragmentView.findViewById(R.id.lv_columnas);
        lv_columnas.setOnItemLongClickListener(this);
        progressBar = fragmentView.findViewById(R.id.column_progress_bar);
        if (columnsAdapter == null) { //el fragment está siendo cargado por primera vez
            progressBar.setVisibility(View.VISIBLE);

            switch (tableType) {
                case TABLE:
                    SQLUtils.getColumns(connectionData, this::onColumnsLoaded, err -> progressBar.setVisibility(View.GONE));
                    break;
                case VIEW:
                    SQLUtils.getViewColumns(connectionData, this::onColumnsLoaded, err -> progressBar.setVisibility(View.GONE));
                    break;
            }
        } else { //ya existe una instancia del fragment
            lv_columnas.setAdapter(columnsAdapter);
        }
    }

    public void onColumnsLoaded(List<SQLColumn> columns) {
        this.columns = columns;
        columnsAdapter = new ListViewImageAdapter<>(getContext(), columns);
        lv_columnas.setAdapter(columnsAdapter);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void implementListeners(View containerView) {

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
        SQLUtils.dropEntity(connectionData, column, nullobj -> {
            columns.remove(column);
            columnsAdapter.notifyDataSetChanged();
            fragmentView.findViewById(R.id.tv_no_columns_exist).setVisibility(columns != null && !columns.isEmpty() ? View.GONE : View.VISIBLE);
        }, err -> Log.e(getClass().getSimpleName(), "Hubo un error al eliminar columna"));
    }
}
