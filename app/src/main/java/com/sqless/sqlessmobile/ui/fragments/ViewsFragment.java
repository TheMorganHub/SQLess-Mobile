package com.sqless.sqlessmobile.ui.fragments;


import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.sqlobjects.SQLView;
import com.sqless.sqlessmobile.ui.activities.TableDetailsActivity;
import com.sqless.sqlessmobile.ui.adapters.listview.ListViewImageAdapter;
import com.sqless.sqlessmobile.utils.FinalValue;
import com.sqless.sqlessmobile.utils.SQLUtils;

import java.util.List;

public class ViewsFragment extends AbstractFragment implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ListViewImageAdapter<SQLView> viewsAdapter;
    private List<SQLView> views;
    private ProgressBar progressBar;
    private ListView lv_views;
    private SwipeRefreshLayout swipeRefreshLayout;

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
        if (viewsAdapter == null) { //el fragment está siendo cargado por primera vez
            progressBar.setVisibility(View.VISIBLE);
            SQLUtils.getViews(connectionData, this::onViewsLoaded, err -> progressBar.setVisibility(View.GONE));
        } else { //ya existe una instancia del fragment
            lv_views.setAdapter(viewsAdapter);
            fragmentView.findViewById(R.id.tv_no_views_exist).setVisibility(views != null && !views.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    public void onViewsLoaded(List<SQLView> views) {
        this.views = views;
        viewsAdapter = new ListViewImageAdapter<>(getContext(), getResources().getDrawable(R.drawable.ic_view), views);
        lv_views.setAdapter(viewsAdapter);
        progressBar.setVisibility(View.GONE);
        fragmentView.findViewById(R.id.tv_no_views_exist).setVisibility(views != null && !views.isEmpty() ? View.GONE : View.VISIBLE);
    }

    public void onViewsRefresh(List<SQLView> views) {
        this.views = views;
        viewsAdapter = new ListViewImageAdapter<>(getContext(), getResources().getDrawable(R.drawable.ic_view), views);
        lv_views.setAdapter(viewsAdapter);
        fragmentView.findViewById(R.id.tv_no_views_exist).setVisibility(views != null && !views.isEmpty() ? View.GONE : View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        SQLUtils.getViews(connectionData, this::onViewsRefresh, err -> swipeRefreshLayout.setRefreshing(false));
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        FinalValue<AlertDialog> dialog = new FinalValue<>();
        AlertDialog.Builder actionDialog = new AlertDialog.Builder(getContext());
        actionDialog.setItems(new String[]{"Eliminar"}, (dialogInterface, clickedItem) -> {
            switch (clickedItem) {
                case 0:
                    deleteView(views.get(i));
                    break;
            }
        });
        dialog.set(actionDialog.show());
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(getContext(), TableDetailsActivity.class);
        connectionData.setTableName(views.get(i).toString());
        intent.putExtra("CONNECTION_DATA", connectionData);
        intent.putExtra("TABLE_TYPE", ColumnsFragment.VIEW);
        startActivity(intent);
    }

    public void deleteView(SQLView view) {
        SQLUtils.dropEntity(connectionData, view, () -> {
            views.remove(view);
            viewsAdapter.notifyDataSetChanged();
            fragmentView.findViewById(R.id.tv_no_views_exist).setVisibility(views != null && !views.isEmpty() ? View.GONE : View.VISIBLE);
        }, err -> Log.e(getClass().getSimpleName(), "Hubo un error al eliminar vista"));
    }

    @Override
    protected void implementListeners(View containerView) {
        lv_views = containerView.findViewById(R.id.lv_views);
        lv_views.setOnItemLongClickListener(this);
        lv_views.setOnItemClickListener(this);
        swipeRefreshLayout = containerView.findViewById(R.id.lay_swipe_refresh_views);
        swipeRefreshLayout.setOnRefreshListener(this);
    }
}
