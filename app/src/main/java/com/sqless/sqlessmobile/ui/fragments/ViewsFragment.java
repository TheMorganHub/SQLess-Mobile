package com.sqless.sqlessmobile.ui.fragments;


import android.content.Intent;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.sqlobjects.SQLView;
import com.sqless.sqlessmobile.ui.activities.TableDetailsActivity;
import com.sqless.sqlessmobile.ui.adapters.listview.ListViewImageAdapter;
import com.sqless.sqlessmobile.utils.FinalValue;
import com.sqless.sqlessmobile.utils.SQLUtils;
import com.sqless.sqlessmobile.utils.UIUtils;

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
            SQLUtils.getViews(getActivity(), connectionData, this::onViewsLoaded, err -> progressBar.setVisibility(View.GONE));
        } else { //ya existe una instancia del fragment
            lv_views.setAdapter(viewsAdapter);
            showOrHideBackground();
        }
    }

    public void onViewsLoaded(List<SQLView> views) {
        this.views = views;
        viewsAdapter = new ListViewImageAdapter<>(getContext(), getResources().getDrawable(R.drawable.ic_view), views);
        lv_views.setAdapter(viewsAdapter);
        progressBar.setVisibility(View.GONE);
        showOrHideBackground();
    }

    public void onViewsRefresh(List<SQLView> views) {
        this.views = views;
        viewsAdapter = new ListViewImageAdapter<>(getContext(), getResources().getDrawable(R.drawable.ic_view), views);
        lv_views.setAdapter(viewsAdapter);
        showOrHideBackground();
        swipeRefreshLayout.setRefreshing(false);
    }

    public void showOrHideBackground() {
        int value = views != null && !views.isEmpty() ? View.GONE : View.VISIBLE;
        fragmentView.findViewById(R.id.tv_no_views_exist).setVisibility(value);
        fragmentView.findViewById(R.id.tv_no_views_exist_2).setVisibility(value);
        fragmentView.findViewById(R.id.iv_no_views_exist).setVisibility(value);
    }

    @Override
    public void onRefresh() {
        SQLUtils.getViews(getActivity(), connectionData, this::onViewsRefresh, err -> swipeRefreshLayout.setRefreshing(false));
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        FinalValue<AlertDialog> dialog = new FinalValue<>();
        AlertDialog.Builder actionDialog = new AlertDialog.Builder(getContext());
        actionDialog.setItems(new String[]{"Renombrar", "Eliminar"}, (dialogInterface, clickedItem) -> {
            switch (clickedItem) {
                case 0:
                    renameView(views.get(i));
                    break;
                case 1:
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
        intent.putExtra("connection_data", connectionData);
        intent.putExtra("table_type", ColumnsFragment.VIEW);
        intent.putExtra("selectable", views.get(i));
        startActivity(intent);
    }

    public void deleteView(SQLView view) {
        UIUtils.showConfirmationDialog(getActivity(), "Eliminar vista", "¿Estás seguro que deseas eliminar la vista " + view.getName() + "?",
                () -> SQLUtils.dropEntity(getActivity(), connectionData, view, () -> {
                    views.remove(view);
                    viewsAdapter.notifyDataSetChanged();
                    showOrHideBackground();
                }, err -> UIUtils.showMessageDialog(getActivity(), "Eliminar vista", "No se pudo eliminar la vista.\nEl servidor respondió:\n" + err)));
    }

    public void renameView(SQLView view) {
        UIUtils.showInputDialog(getActivity(), "Renombrar " + view.getName(), nombre -> {
            SQLUtils.renameEntity(getActivity(), connectionData, nombre, view, () -> {
                view.setName(nombre);
                viewsAdapter.notifyDataSetChanged();
            }, err -> UIUtils.showMessageDialog(getActivity(), "Renombrar " + view.getName(), "Hubo un error al renombrar entidad: " + err));
        }, () -> Toast.makeText(getActivity(), "El nombre de la tabla no puede estar vacío.", Toast.LENGTH_SHORT).show());
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
