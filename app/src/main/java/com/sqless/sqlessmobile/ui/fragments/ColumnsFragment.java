package com.sqless.sqlessmobile.ui.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.network.SQLConnectionManager;
import com.sqless.sqlessmobile.sqlobjects.SQLColumn;
import com.sqless.sqlessmobile.sqlobjects.SQLObject;
import com.sqless.sqlessmobile.sqlobjects.SQLSelectable;
import com.sqless.sqlessmobile.ui.activities.QueryResultActivity;
import com.sqless.sqlessmobile.ui.adapters.listview.ListViewImageAdapter;
import com.sqless.sqlessmobile.ui.busevents.tabledata.DataEvents;
import com.sqless.sqlessmobile.utils.Callback;
import com.sqless.sqlessmobile.utils.FinalValue;
import com.sqless.sqlessmobile.utils.SQLUtils;
import com.sqless.sqlessmobile.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Un fragment que se encarga de mostrar columnas de vistas y tablas. Uno de los argumentos que
 * llevará este fragment es el de {@code table_type}. Si {@code table_type} es {@code TABLE},
 * la clase cargará las columnas llamando al método {@link SQLUtils#getColumns(Activity, SQLObject, SQLConnectionManager.ConnectionData, Callback, Callback)} .
 * De lo contrario si {@code table_type} es {@code VIEW}, la clase cargará las columnas llamando
 * a {@link SQLUtils#getViewColumns(Activity, SQLObject, SQLConnectionManager.ConnectionData, Callback, Callback)} .
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
    EventBus bus = EventBus.getDefault();

    public ColumnsFragment() {
        // Required empty public constructor
    }

    @Override
    public void afterCreate() {
        if (!bus.isRegistered(this)) {
            bus.register(this);
        }
        final int tableType = getArguments().getInt("table_type", -1);
        progressBar = fragmentView.findViewById(R.id.column_progress_bar);
        SQLObject selectable = (SQLObject) getArguments().getSerializable("selectable");

        if (columnsAdapter == null) { //el fragment está siendo cargado por primera vez
            progressBar.setVisibility(View.VISIBLE);
            switch (tableType) {
                case TABLE:
                    SQLUtils.getColumns(getActivity(), selectable, connectionData, this::onColumnsLoaded, err -> {
                        progressBar.setVisibility(View.GONE);
                    });
                    break;
                case VIEW:
                    SQLUtils.getViewColumns(getActivity(), selectable, connectionData, this::onColumnsLoaded, err -> progressBar.setVisibility(View.GONE));
                    break;
            }
        } else { //ya existe una instancia del fragment
            lv_columnas.setAdapter(columnsAdapter);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (bus.isRegistered(this)) {
            bus.unregister(this);
        }
    }

    @Subscribe
    public void onColumnsRequestEvent(DataEvents.TableColumnsRequestEvent event) {
        bus.post(new DataEvents.TableColumnsResponseEvent(columns));
    }

    @Override
    public void onRefresh() {
        final int tableType = getArguments().getInt("table_type", -1);
        SQLObject selectable = (SQLObject) getArguments().getSerializable("selectable");
        switch (tableType) {
            case TABLE:
                SQLUtils.getColumns(getActivity(), selectable, connectionData, this::onColumnsRefresh, err -> swipeRefreshLayout.setRefreshing(false));
                break;
            case VIEW:
                SQLUtils.getViewColumns(getActivity(), selectable, connectionData, this::onColumnsRefresh, err -> swipeRefreshLayout.setRefreshing(false));
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
        SQLObject selectable = (SQLObject) getArguments().getSerializable("selectable");
        return selectable != null ? selectable.getName() : "";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_columns;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (getArguments().getInt("table_type", -1) == VIEW) {
            return false;
        }
        FinalValue<AlertDialog> dialog = new FinalValue<>();
        AlertDialog.Builder actionDialog = new AlertDialog.Builder(getContext());
        actionDialog.setItems(new String[]{"Editar", "Eliminar"}, (dialogInterface, clickedItem) -> {
            switch (clickedItem) {
                case 0:
                    showEditColumnDialog(columns.get(position));
                    break;
                case 1:
                    deleteColumn(columns.get(position));
                    break;
            }
        });
        dialog.set(actionDialog.show());
        return true;
    }

    public void showEditColumnDialog(SQLColumn column) {
        SQLColumn editedColumn = new SQLColumn(column);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_column, fragmentView.findViewById(android.R.id.content), false);
        builder.setPositiveButton("Confirmar", null);
        builder.setNegativeButton("Cancelar", null);
        builder.setView(viewInflated);
        builder.setTitle("Editar columna");
        EditText txtColName = viewInflated.findViewById(R.id.txt_edit_col_name);
        Spinner spDataType = viewInflated.findViewById(R.id.sp_edit_column_datatype);
        Switch switchNullable = viewInflated.findViewById(R.id.switch_nullable);
        txtColName.setText(editedColumn.getName());
        if (!SQLUtils.datatypeIsSupported(editedColumn.getDatatype())) {
            List<String> datatypes = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.arr_create_table_datatypes)));
            datatypes.add(editedColumn.getDatatype());
            spDataType.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, datatypes));
        }
        switchNullable.setChecked(editedColumn.isNullable());
        UIUtils.selectSpinnerItemByValue(spDataType, editedColumn.getDatatype());
        spDataType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstLoad = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!firstLoad) {
                    editedColumn.setDatatype(getActivity(), connectionData, spDataType.getItemAtPosition(position).toString());
                }
                firstLoad = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        activeDialog = builder.show();
        activeDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newName = txtColName.getText().toString();
            if (newName.isEmpty()) {
                Toast.makeText(getActivity(), "El nombre de la columna no puede estar vacío.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean nullable = switchNullable.isChecked();
            editedColumn.setUncommittedName(newName);
            editedColumn.setNullable(nullable);
            if (!editedColumn.getFirstTimeChangeStatement().equals(editedColumn.getChangeColumnStatement())) {
                SQLUtils.updateColumn(getActivity(), connectionData, editedColumn, () -> {
                    if (!editedColumn.getUncommittedName().equals(editedColumn.getName())) {
                        editedColumn.setName(editedColumn.getUncommittedName());
                    }
                    columns.remove(column);
                    columns.add(editedColumn);
                    columnsAdapter.notifyDataSetChanged();
                }, err -> UIUtils.showMessageDialog(getActivity(), "Editar columna", "No se pudo editar la columna.\nEl servidor respondió:\n" + err));
            }
            activeDialog.dismiss();
        });
    }

    public void deleteColumn(SQLColumn column) {
        UIUtils.showConfirmationDialog(getActivity(), "Eliminar columna", "¿Estás seguro que deseas eliminar la columna " + column.getName() + "?",
                () -> SQLUtils.dropEntity(getActivity(), connectionData, column, () -> {
                    columns.remove(column);
                    columnsAdapter.notifyDataSetChanged();
                    fragmentView.findViewById(R.id.tv_no_columns_exist).setVisibility(columns != null && !columns.isEmpty() ? View.GONE : View.VISIBLE);
                }, err -> UIUtils.showMessageDialog(getActivity(), "Eliminar columna", "No se pudo eliminar la columna.\nEl servidor respondió:\n" + err)));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SQLColumn column = columns.get(position);
        Intent intent = new Intent(getContext(), QueryResultActivity.class);
        intent.putExtra("connection_data", connectionData);
        intent.putExtra("query_title", column.getParentName() + "." + column.getName());
        intent.putExtra("query_to_run", column.getSelectStatement(200));
        intent.putExtra("query_to_export", column.getSelectStatement(SQLSelectable.ALL));
        intent.putExtra("result_name", column.getParentName() + "_" + column.getName());
        startActivity(intent);
    }
}
