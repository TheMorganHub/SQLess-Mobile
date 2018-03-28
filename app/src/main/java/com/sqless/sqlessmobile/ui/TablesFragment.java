package com.sqless.sqlessmobile.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.adapters.ListViewImageAdapter;
import com.sqless.sqlessmobile.utils.SQLUtils;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TablesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TablesFragment extends AbstractFragment implements AdapterView.OnItemClickListener {

    private List<String> tableNames;
    private ListViewImageAdapter<String> tablesAdapter;

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
        ProgressBar progressBar = fragmentView.findViewById(R.id.progress_bar);
        if (tablesAdapter == null) { //el fragment está siendo cargado por primera vez
            progressBar.setVisibility(View.VISIBLE);
            SQLUtils.getTableNames(connectionData, true, names -> {
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
        containerView.findViewById(R.id.fab_create_table).setOnClickListener(view1 -> {
            Log.i("FAB", "Bwerk!");
            //TODO implement listener
        });
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
        startActivity(intent);
    }
}
