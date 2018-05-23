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
import com.sqless.sqlessmobile.sqlobjects.SQLTable;
import com.sqless.sqlessmobile.ui.activities.CreateTableActivity;
import com.sqless.sqlessmobile.ui.activities.TableDetailsActivity;
import com.sqless.sqlessmobile.ui.adapters.listview.ListViewImageAdapter;
import com.sqless.sqlessmobile.utils.FinalValue;
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
            SQLUtils.getTables(connectionData, this::onTablesLoaded, err -> progressBar.setVisibility(View.GONE));
        } else { //ya existe una instancia del fragment
            lv_tables.setAdapter(tablesAdapter);
        }
    }

    @Override
    public void onRefresh() {
        SQLUtils.getTables(connectionData, this::onTablesRefresh, err -> swipeRefreshLayout.setRefreshing(false));
    }

    public void onTablesRefresh(List<SQLTable> tables) {
        this.tables = tables;
        tablesAdapter = new ListViewImageAdapter<>(getContext(), getResources().getDrawable(R.drawable.ic_table_black_24dp), tables);
        lv_tables.setAdapter(tablesAdapter);

        swipeRefreshLayout.setRefreshing(false);
        fragmentView.findViewById(R.id.tv_no_tables_exist).setVisibility(tables != null && !tables.isEmpty() ? View.GONE : View.VISIBLE);
    }

    public void onTablesLoaded(List<SQLTable> tables) {
        this.tables = tables;
        tablesAdapter = new ListViewImageAdapter<>(getContext(), getResources().getDrawable(R.drawable.ic_table_black_24dp), tables);
        lv_tables.setAdapter(tablesAdapter);
        progressBar.setVisibility(View.GONE);
        fragmentView.findViewById(R.id.tv_no_tables_exist).setVisibility(tables != null && !tables.isEmpty() ? View.GONE : View.VISIBLE);
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

    public void actionCreateTable() {
        Intent intent = new Intent(getContext(), CreateTableActivity.class);
        intent.putExtra("CONNECTION_DATA", connectionData);
        startActivityForResult(intent, TABLE_CREATION_RESULT);
    }

    public void deleteTable(SQLTable table) {
        SQLUtils.dropEntity(connectionData, table, () -> {
            tables.remove(table);
            tablesAdapter.notifyDataSetChanged();
            fragmentView.findViewById(R.id.tv_no_tables_exist).setVisibility(tables != null && !tables.isEmpty() ? View.GONE : View.VISIBLE);
        }, err -> Log.e(getClass().getSimpleName(), "Hubo un error al eliminar tabla"));
    }

    public void renameTable(SQLTable table) {
        UIUtils.showInputDialog(getActivity(), "Renombrar " + table.getName(), nombre -> {
            SQLUtils.renameEntity(connectionData, nombre, table, () -> {
                table.setName(nombre);
                tablesAdapter.notifyDataSetChanged();
            }, err -> UIUtils.showMessageDialog(getActivity(), "Renombrar " + table.getName(), "Hubo un error al renombrar entidad: " + err));
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TABLE_CREATION_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    SQLTable newTable = (SQLTable) data.getSerializableExtra("NEW_TABLE");
                    tables.add(newTable);
                    tablesAdapter.notifyDataSetChanged();
                    fragmentView.findViewById(R.id.tv_no_tables_exist).setVisibility(tables != null && !tables.isEmpty() ? View.GONE : View.VISIBLE);
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
        connectionData.setTableName(tables.get(i).getName());
        intent.putExtra("CONNECTION_DATA", connectionData);
        intent.putExtra("TABLE_TYPE", ColumnsFragment.TABLE);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        AlertDialog.Builder actionDialog = new AlertDialog.Builder(getContext());
        actionDialog.setItems(new String[]{"Eliminar", "Renombrar"}, (dialogInterface, clickedItem) -> {
            switch (clickedItem) {
                case 0:
                    deleteTable(tables.get(i));
                    break;
                case 1:
                    renameTable(tables.get(i));
                    break;
            }
        });
        activeDialog = actionDialog.show();
        return true;
    }
}
