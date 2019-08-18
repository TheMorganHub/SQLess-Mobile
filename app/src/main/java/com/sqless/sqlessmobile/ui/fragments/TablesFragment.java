package com.sqless.sqlessmobile.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.sqlobjects.SQLTable;
import com.sqless.sqlessmobile.ui.activities.CreateTableActivity;
import com.sqless.sqlessmobile.ui.activities.TableDetailsActivity;
import com.sqless.sqlessmobile.ui.adapters.listview.ListViewImageAdapter;
import com.sqless.sqlessmobile.utils.SQLUtils;
import com.sqless.sqlessmobile.utils.UIUtils;

import java.util.List;

public class TablesFragment extends AbstractFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, SwipeRefreshLayout.OnRefreshListener {

    private List<SQLTable> tables;
    private ListViewImageAdapter<SQLTable> tablesAdapter;
    private ListView lv_tables;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private static final int TABLE_CREATION_RESULT = 341;

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
    public void afterCreate() {
        progressBar = fragmentView.findViewById(R.id.progress_bar_tables);
        if (tablesAdapter == null) { //el fragment está siendo cargado por primera vez
            progressBar.setVisibility(View.VISIBLE);
            SQLUtils.getTables(getActivity(), connectionData, this::onTablesLoaded, err -> progressBar.setVisibility(View.GONE));
        } else { //ya existe una instancia del fragment
            lv_tables.setAdapter(tablesAdapter);
            showOrHideBackground();
        }
    }

    @Override
    public void onRefresh() {
        SQLUtils.getTables(getActivity(), connectionData, this::onTablesRefresh, err -> swipeRefreshLayout.setRefreshing(false));
    }

    public void onTablesRefresh(List<SQLTable> tables) {
        this.tables = tables;
        tablesAdapter = new ListViewImageAdapter<>(getContext(), getResources().getDrawable(R.drawable.ic_table_black_24dp), tables);
        lv_tables.setAdapter(tablesAdapter);

        swipeRefreshLayout.setRefreshing(false);
        showOrHideBackground();
    }

    public void onTablesLoaded(List<SQLTable> tables) {
        this.tables = tables;
        tablesAdapter = new ListViewImageAdapter<>(getContext(), getResources().getDrawable(R.drawable.ic_table_black_24dp), tables);
        lv_tables.setAdapter(tablesAdapter);
        progressBar.setVisibility(View.GONE);
        showOrHideBackground();
    }

    @Override
    protected void implementListeners(View containerView) {
        containerView.findViewById(R.id.fab_create_table).setOnClickListener(view1 -> actionCreateTable());
        swipeRefreshLayout = containerView.findViewById(R.id.lay_swipe_refresh_tables);
        swipeRefreshLayout.setOnRefreshListener(this);
        lv_tables = containerView.findViewById(R.id.lv_tables);
        lv_tables.setOnItemClickListener(this);
        lv_tables.setOnItemLongClickListener(this);
    }

    public void showOrHideBackground() {
        int value = tables != null && !tables.isEmpty() ? View.GONE : View.VISIBLE;
        fragmentView.findViewById(R.id.tv_no_tables_exist).setVisibility(value);
        fragmentView.findViewById(R.id.tv_no_tables_exist_2).setVisibility(value);
        fragmentView.findViewById(R.id.iv_no_tables_exist).setVisibility(value);
    }

    public void actionCreateTable() {
        Intent intent = new Intent(getContext(), CreateTableActivity.class);
        intent.putExtra("connection_data", connectionData);
        startActivityForResult(intent, TABLE_CREATION_RESULT);
    }

    public void deleteTable(SQLTable table) {
        UIUtils.showConfirmationDialog(getActivity(), "Eliminar tabla", "¿Estás seguro que deseas eliminar la tabla " + table.getName() + "?",
                () -> SQLUtils.dropEntity(getActivity(), connectionData, table, () -> {
                    tables.remove(table);
                    tablesAdapter.notifyDataSetChanged();
                    showOrHideBackground();
                }, err -> UIUtils.showMessageDialog(getActivity(), "Eliminar tabla", "No se pudo eliminar la tabla.\nEl servidor respondió:\n" + err)));
    }

    public void renameTable(SQLTable table) {
        UIUtils.showInputDialog(getActivity(), "Renombrar " + table.getName(), nombre -> {
            SQLUtils.renameEntity(getActivity(), connectionData, nombre, table, () -> {
                table.setName(nombre);
                tablesAdapter.notifyDataSetChanged();
            }, err -> UIUtils.showMessageDialog(getActivity(), "Renombrar " + table.getName(), "Hubo un error al renombrar entidad: " + err));
        }, () -> Toast.makeText(getActivity(), "El nombre de la tabla no puede estar vacío.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TABLE_CREATION_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    SQLTable newTable = (SQLTable) data.getSerializableExtra("new_table");
                    tables.add(newTable);
                    tablesAdapter.notifyDataSetChanged();
                    showOrHideBackground();
                }
                break;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(getContext(), TableDetailsActivity.class);
        intent.putExtra("connection_data", connectionData);
        intent.putExtra("table_type", ColumnsFragment.TABLE);
        intent.putExtra("selectable", tables.get(i));
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        AlertDialog.Builder actionDialog = new AlertDialog.Builder(getContext());
        actionDialog.setItems(new String[]{"Renombrar", "Eliminar"}, (dialogInterface, clickedItem) -> {
            switch (clickedItem) {
                case 0:
                    renameTable(tables.get(i));
                    break;
                case 1:
                    deleteTable(tables.get(i));
                    break;
            }
        });
        activeDialog = actionDialog.show();
        return true;
    }
}
