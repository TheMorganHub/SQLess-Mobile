package com.sqless.sqlessmobile.ui.fragments;

import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.sqlobjects.SQLColumn;
import com.sqless.sqlessmobile.ui.adapters.listview.ListViewImageAdapter;
import com.sqless.sqlessmobile.utils.SQLUtils;

import java.util.List;

public class ColumnsFragment extends AbstractFragment {

    private List<SQLColumn> columns;
    private ListViewImageAdapter<SQLColumn> columnsAdapter;

    public ColumnsFragment() {
        // Required empty public constructor
    }

    @Override
    public void afterCreate() {
        ListView lv_columnas = fragmentView.findViewById(R.id.lv_columnas);
        ProgressBar progressBar = fragmentView.findViewById(R.id.column_progress_bar);
        if (columnsAdapter == null) { //el fragment está siendo cargado por primera vez
            progressBar.setVisibility(View.VISIBLE);
            SQLUtils.getColumns(connectionData, sqlColumns -> {
                columns = sqlColumns;
                columnsAdapter = new ListViewImageAdapter<>(getContext(), columns);
                lv_columnas.setAdapter(columnsAdapter);
                progressBar.setVisibility(View.GONE);
            }, err -> progressBar.setVisibility(View.GONE));
        } else { //ya existe una instancia del fragment
            lv_columnas.setAdapter(columnsAdapter);
        }
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

}
