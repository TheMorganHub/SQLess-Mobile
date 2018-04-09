package com.sqless.sqlessmobile.ui.fragments;


import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.activities.TableDetailsActivity;
import com.sqless.sqlessmobile.ui.adapters.listview.ListViewImageAdapter;
import com.sqless.sqlessmobile.utils.FinalValue;
import com.sqless.sqlessmobile.utils.SQLUtils;

import java.util.List;

public class ViewsFragment extends AbstractFragment implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    private ListViewImageAdapter<String> viewsAdapter;
    private List<String> viewNames;
    private ProgressBar progressBar;
    private ListView lv_views;


    public ViewsFragment() {
        // Required empty public constructor
    }


    @Override
    protected String getTitle() {
        return "Vistas";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_views;
    }

    @Override
    public void afterCreate() {
        progressBar = fragmentView.findViewById(R.id.progress_bar_views);
        lv_views = fragmentView.findViewById(R.id.lv_views);
        lv_views.setOnItemLongClickListener(this);
        lv_views.setOnItemClickListener(this);

        if (viewsAdapter == null) { //el fragment está siendo cargado por primera vez
            progressBar.setVisibility(View.VISIBLE);
            SQLUtils.getViewNames(connectionData, this::onViewsLoaded, err -> progressBar.setVisibility(View.GONE));
        } else { //ya existe una instancia del fragment
            lv_views.setAdapter(viewsAdapter);
        }
    }

    public void onViewsLoaded(List<String> viewNames) {
        this.viewNames = viewNames;
        viewsAdapter = new ListViewImageAdapter<>(getContext(), getResources().getDrawable(R.drawable.ic_view), viewNames);
        lv_views.setAdapter(viewsAdapter);
        progressBar.setVisibility(View.GONE);
        fragmentView.findViewById(R.id.tv_no_views_exist).setVisibility(viewNames != null && !viewNames.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        FinalValue<AlertDialog> dialog = new FinalValue<>();
        AlertDialog.Builder actionDialog = new AlertDialog.Builder(getContext());
        actionDialog.setItems(new String[]{"Eliminar"}, (dialogInterface, clickedItem) -> {
            switch (clickedItem) {
                case 0:
                    deleteView(viewNames.get(i));
                    break;
            }
        });
        dialog.set(actionDialog.show());
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(getContext(), TableDetailsActivity.class);
        connectionData.setTableName(viewNames.get(i));
        intent.putExtra("CONNECTION_DATA", connectionData);
        intent.putExtra("TABLE_TYPE", ColumnsFragment.VIEW);
        startActivity(intent);
    }

    public void deleteView(String name) {
        //TODO delete view
    }

    @Override
    protected void implementListeners(View containerView) {

    }
}
