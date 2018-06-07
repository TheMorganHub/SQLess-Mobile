package com.sqless.sqlessmobile.ui.fragments;


import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.sqlobjects.SQLColumn;
import com.sqless.sqlessmobile.ui.adapters.listview.ListViewColumnDetailsAdapter;
import com.sqless.sqlessmobile.ui.busevents.createtable.ColumnEvents;
import com.sqless.sqlessmobile.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class CreateColumnsFragment extends AbstractFragment implements AdapterView.OnItemLongClickListener {

    private List<SQLColumn> sqlColumns;
    private ListViewColumnDetailsAdapter adapter;
    EventBus bus = EventBus.getDefault();


    public CreateColumnsFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getTitle() {
        return "Crear tabla";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_create_columns;
    }

    @Override
    public void afterCreate() {
        ListView lvCreateTableColumns = fragmentView.findViewById(R.id.lv_create_table_columns);
        if (adapter == null) {
            sqlColumns = new ArrayList<>();
            adapter = new ListViewColumnDetailsAdapter(getContext(), sqlColumns);
        }
        lvCreateTableColumns.setAdapter(adapter);
        fragmentView.findViewById(R.id.tv_create_table_no_columns).setVisibility(sqlColumns.isEmpty() ? View.VISIBLE : View.INVISIBLE);
    }

    public void doCreateColummn(View dialogView) {
        String nombre = ((TextView) dialogView.findViewById(R.id.txt_create_table_col_name)).getText().toString();
        String dataType = ((Spinner) dialogView.findViewById(R.id.sp_create_table_col_datatype)).getSelectedItem().toString();
        boolean isPk = ((Switch) dialogView.findViewById(R.id.switch_pk)).isChecked();
        boolean nullable = ((Switch) dialogView.findViewById(R.id.switch_nullable)).isChecked();
        SQLColumn newColumn = new SQLColumn("", nombre, dataType, isPk, nullable);
        sqlColumns.add(newColumn);
        adapter.notifyDataSetChanged();

        if (!sqlColumns.isEmpty()) {
            fragmentView.findViewById(R.id.tv_create_table_no_columns).setVisibility(View.INVISIBLE);
        }

        activeDialog.dismiss();
        bus.post(new ColumnEvents.ColumnAddedEvent(newColumn));
    }

    public void createNewColumnDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.new_column_dialog, fragmentView.findViewById(android.R.id.content), false);
        dialogBuilder.setPositiveButton("Crear", (dialog, which) -> doCreateColummn(viewInflated));

        dialogBuilder.setView(viewInflated);
        dialogBuilder.setTitle("Nueva columna");
        activeDialog = dialogBuilder.show();
    }

    private void deleteColumn(SQLColumn column) {
        sqlColumns.remove(column);
        adapter.notifyDataSetChanged();
        bus.post(new ColumnEvents.ColumnRemovedEvent(column));
        if (sqlColumns.isEmpty()) {
            fragmentView.findViewById(R.id.tv_create_table_no_columns).setVisibility(View.VISIBLE);
        }
    }

    private void renameColumn(SQLColumn column) {
        UIUtils.showInputDialog(getActivity(), "Renombrar " + column.getName(), newName -> {
            column.setName(newName);
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        AlertDialog.Builder actionDialog = new AlertDialog.Builder(getContext());
        actionDialog.setItems(new String[]{"Eliminar", "Renombrar"}, (dialogInterface, clickedItem) -> {
            switch (clickedItem) {
                case 0:
                    deleteColumn(sqlColumns.get(position));
                    break;
                case 1:
                    renameColumn(sqlColumns.get(position));
                    break;
            }
        });
        activeDialog = actionDialog.show();
        return true;
    }

    @Override
    public void onFabClicked() {
        createNewColumnDialog();
    }

    @Override
    protected void implementListeners(View containerView) {
        ListView lvCreateTableColumns = fragmentView.findViewById(R.id.lv_create_table_columns);
        lvCreateTableColumns.setOnItemLongClickListener(this);
    }
}
