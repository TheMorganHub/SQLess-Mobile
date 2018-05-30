package com.sqless.sqlessmobile.ui.fragments;

import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.sqlobjects.SQLColumn;
import com.sqless.sqlessmobile.sqlobjects.SQLForeignKey;
import com.sqless.sqlessmobile.sqlobjects.SQLTable;
import com.sqless.sqlessmobile.ui.adapters.listview.ListViewImageAdapter;
import com.sqless.sqlessmobile.ui.busevents.createtable.ColumnEvents;
import com.sqless.sqlessmobile.utils.SQLUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class CreateUnionFragment extends AbstractFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, AdapterView.OnItemLongClickListener {

    private List<SQLForeignKey> foreignKeys;
    private ListViewImageAdapter<SQLForeignKey> adapter;
    EventBus bus = EventBus.getDefault();
    private List<SQLColumn> columns;
    private ArrayAdapter<SQLColumn> spinnerFkColumnAdapter;
    private ArrayAdapter<SQLTable> spinnerRefTableAdapter;
    private ArrayAdapter<String> spinnerRefColumnAdapter;


    public CreateUnionFragment() {
        // Required empty public constructor
    }


    @Override
    protected String getTitle() {
        return "Crear tabla";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_create_union;
    }

    @Override
    public void afterCreate() {
        ListView lvUnions = fragmentView.findViewById(R.id.lv_unions);

        if (adapter == null) {
            foreignKeys = new ArrayList<>();
            adapter = new ListViewImageAdapter<>(getContext(), getResources().getDrawable(R.drawable.ic_foreign_key_24dp), foreignKeys);
        }
        if (!bus.isRegistered(this)) {
            bus.register(this);
        }
        lvUnions.setAdapter(adapter);
        fragmentView.findViewById(R.id.tv_create_table_no_fks).setVisibility(foreignKeys.isEmpty() ? View.VISIBLE : View.INVISIBLE);
    }

    public void createNewFKDialog() {
        bus.post(new ColumnEvents.ColumnRequestEvent()); //pedimos las columnas desde CreateTableActivity

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.new_fk_dialog, fragmentView.findViewById(android.R.id.content), false);
        Button btnCrear = viewInflated.findViewById(R.id.btn_create_fk_confirm);
        btnCrear.setOnClickListener(this);

        if (columns != null) {
            Spinner sp = viewInflated.findViewById(R.id.sp_fk_col_name);
            spinnerFkColumnAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, columns);
            sp.setAdapter(spinnerFkColumnAdapter);
        }

        dialogBuilder.setView(viewInflated);
        dialogBuilder.setTitle("Nueva FK");
        activeDialog = dialogBuilder.show();

        Spinner tablesSpinner = viewInflated.findViewById(R.id.sp_fk_ref_table);

        ProgressBar progressBar = viewInflated.findViewById(R.id.progress_ref_table);
        progressBar.setVisibility(View.VISIBLE);
        SQLUtils.getTables(getActivity(), connectionData, names -> {
            if (activeDialog != null && activeDialog.isShowing()) {
                spinnerRefTableAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, names);
                tablesSpinner.setAdapter(spinnerRefTableAdapter);
                tablesSpinner.setOnItemSelectedListener(this);
                progressBar.setVisibility(View.INVISIBLE);
            }
        }, err -> {
            if (activeDialog != null && activeDialog.isShowing()) {
                progressBar.setVisibility(View.INVISIBLE);
            }
            Log.e("ERR", err);
        });
    }

    @Subscribe
    public void onColumnsReceivedEvent(ColumnEvents.ColumnsReceivedEvent event) {
        this.columns = event.columns;
    }

    @Override
    public void onFabClicked() {
        createNewFKDialog();
    }

    @Override
    public void onClick(View view) {
        View inflatedView = view.getRootView();
        String fkNombre = ((EditText) inflatedView.findViewById(R.id.txt_fk_name)).getText().toString();
        Object objFkColumn = ((Spinner) inflatedView.findViewById(R.id.sp_fk_col_name)).getSelectedItem();
        Object objFkRefTableName = ((Spinner) inflatedView.findViewById(R.id.sp_fk_ref_table)).getSelectedItem();
        Object objRefColName = ((Spinner) inflatedView.findViewById(R.id.sp_fk_ref_col)).getSelectedItem();

        if (fkNombre.isEmpty() || objFkColumn == null || objFkRefTableName == null || objRefColName == null) {
            Toast.makeText(view.getContext(), "Ninguno de los campos puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        String fkColumna = ((SQLColumn) objFkColumn).getName();
        String fkRefTableName = objFkRefTableName.toString();
        String fkRefColName = objRefColName.toString();

        SQLForeignKey newFk = new SQLForeignKey(fkNombre, fkColumna, fkRefTableName, fkRefColName);
        foreignKeys.add(newFk);
        adapter.notifyDataSetChanged();

        if (!foreignKeys.isEmpty()) {
            fragmentView.findViewById(R.id.tv_create_table_no_fks).setVisibility(View.INVISIBLE);
        }

        activeDialog.dismiss();
        bus.post(new ColumnEvents.FKAddedEvent(newFk));
    }

    @Override
    protected void implementListeners(View containerView) {
        ListView lvUnions = containerView.findViewById(R.id.lv_unions);
        lvUnions.setOnItemLongClickListener(this);
    }

    public void deleteFk(SQLForeignKey fk) {
        bus.post(new ColumnEvents.FKRemovedEvent(fk));
        foreignKeys.remove(fk);
        adapter.notifyDataSetChanged();
        if (foreignKeys.isEmpty()) {
            fragmentView.findViewById(R.id.tv_create_table_no_fks).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        AlertDialog.Builder actionDialog = new AlertDialog.Builder(getContext());
        actionDialog.setItems(new String[]{"Eliminar"}, (dialogInterface, clickedItem) -> {
            switch (clickedItem) {
                case 0:
                    deleteFk(foreignKeys.get(position));
                    break;
            }
        });
        activeDialog = actionDialog.show();
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        View dialogInflatedView = view.getRootView();
        ProgressBar progressBar = dialogInflatedView.findViewById(R.id.progress_ref_column);
        progressBar.setVisibility(View.VISIBLE);
        String tableNameSelected = spinnerRefTableAdapter.getItem(i).getName();
        Spinner refColumnsSpinner = dialogInflatedView.findViewById(R.id.sp_fk_ref_col);
        SQLUtils.getColumnNamesInTable(getActivity(), connectionData, tableNameSelected, names -> {
            if (activeDialog != null && activeDialog.isShowing()) {
                spinnerRefColumnAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, names);
                refColumnsSpinner.setAdapter(spinnerRefColumnAdapter);
                progressBar.setVisibility(View.INVISIBLE);
            }
        }, err -> {
            if (activeDialog != null && activeDialog.isShowing()) {
                progressBar.setVisibility(View.INVISIBLE);
            }
            Log.e("ERR", err);
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (bus.isRegistered(this)) {
            bus.unregister(this);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
