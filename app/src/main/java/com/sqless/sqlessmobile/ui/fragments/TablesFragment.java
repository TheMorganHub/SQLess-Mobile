package com.sqless.sqlessmobile.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
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

import java.util.List;

public class TablesFragment extends AbstractFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private List<String> tableNames;
    private ListViewImageAdapter<String> tablesAdapter;

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
        ListView lv_tables = fragmentView.findViewById(R.id.lv_tables);
        lv_tables.setOnItemClickListener(this);
        lv_tables.setOnItemLongClickListener(this);
        ProgressBar progressBar = fragmentView.findViewById(R.id.progress_bar_tables);
        if (tablesAdapter == null) { //el fragment está siendo cargado por primera vez
            progressBar.setVisibility(View.VISIBLE);
            SQLUtils.getTableNames(connectionData, names -> {
                tableNames = names;
                tablesAdapter = new ListViewImageAdapter<>(getContext(), getResources().getDrawable(R.drawable.ic_table_black_24dp), tableNames);
                lv_tables.setAdapter(tablesAdapter);
                progressBar.setVisibility(View.GONE);
            }, err -> progressBar.setVisibility(View.GONE));
        } else { //ya existe una instancia del fragment
            lv_tables.setAdapter(tablesAdapter);
        }
    }

    @Override
    protected void implementListeners(View containerView) {
        containerView.findViewById(R.id.fab_create_table).setOnClickListener(view1 -> actionCreateTable());
    }

    public void actionCreateTable() {
        Intent intent = new Intent(getContext(), CreateTableActivity.class);
        intent.putExtra("CONNECTION_DATA", connectionData);
        startActivityForResult(intent, TABLE_CREATION_RESULT);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        FinalValue<AlertDialog> dialog = new FinalValue<>();
        AlertDialog.Builder actionDialog = new AlertDialog.Builder(getContext());
        actionDialog.setItems(new String[]{"Eliminar"}, (dialogInterface, clickedItem) -> {
            switch (clickedItem) {
                case 0:
                    deleteTable(tableNames.get(i));
                    break;
            }
        });
        dialog.set(actionDialog.show());
        return true;
    }

    public void deleteTable(String name) {
        //TODO delete table
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TABLE_CREATION_RESULT:
                if (resultCode == Activity.RESULT_OK) {
                    SQLTable newTable = (SQLTable) data.getSerializableExtra("NEW_TABLE");
                    tableNames.add(newTable.getName());
                    tablesAdapter.notifyDataSetChanged();
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
        connectionData.setTableName(tableNames.get(i));
        intent.putExtra("CONNECTION_DATA", connectionData);
        intent.putExtra("TABLE_TYPE", ColumnsFragment.TABLE);
        startActivity(intent);
    }
}
