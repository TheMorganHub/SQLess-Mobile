package com.sqless.sqlessmobile.ui.fragments;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.busevents.tabledata.DataEvents;
import com.sqless.sqlessmobile.utils.SQLUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class TableHtmlFragment extends AbstractFragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    EventBus bus = EventBus.getDefault();


    public TableHtmlFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getTitle() {
        return getArguments().getString("query_title");
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_table_html;
    }

    @Override
    public void afterCreate() {
        if (!bus.isRegistered(this)) {
            bus.register(this);
        }
        String queryContents = getArguments().getString("query_to_run");
        if (queryContents != null) {
            createHTMLTable(queryContents);
        }
    }

    public void createHTMLTable(String sql) {
        TextView tvError = fragmentView.findViewById(R.id.tv_table_html_error);
        WebView wv = fragmentView.findViewById(R.id.wv_table);
        ProgressBar progressBar = fragmentView.findViewById(R.id.progress_bar_html_fragment);

        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setDisplayZoomControls(false);
        wv.getSettings().setDefaultTextEncodingName("utf-8");

        progressBar.setVisibility(View.VISIBLE);
        wv.setVisibility(View.INVISIBLE);
        SQLUtils.createHTMLFromQueryResult(getActivity(), connectionData, sql,
                doc -> {
                    wv.setVisibility(View.VISIBLE);
                    wv.loadDataWithBaseURL(doc.getAssetsFolder(), doc.getHTML(), "text/html", "utf-8", null);
                    progressBar.setVisibility(View.INVISIBLE);
                },
                err -> {
                    tvError.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    tvError.setText("Hubo un error al ejecutar la consulta dada.\nEl servidor respondió con mensaje:\n" + err);
                });
    }


    @Override
    protected void implementListeners(View containerView) {
        swipeRefreshLayout = containerView.findViewById(R.id.lay_swipe_refresh_html);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Subscribe
    public void onRefreshDataRequestEvent(DataEvents.RefreshDataRequestEvent event) {
        doRefresh();
    }

    @Override
    public void onRefresh() {
        doRefresh();
    }

    private void doRefresh() {
        String queryContents = getArguments().getString("query_to_run");
        if (queryContents != null) {
            TextView tvError = fragmentView.findViewById(R.id.tv_table_html_error);
            WebView wv = fragmentView.findViewById(R.id.wv_table);
            SQLUtils.createHTMLFromQueryResult(getActivity(), connectionData, queryContents,
                    doc -> {
                        wv.setVisibility(View.VISIBLE);
                        tvError.setVisibility(View.INVISIBLE);
                        wv.loadDataWithBaseURL(doc.getAssetsFolder(), doc.getHTML(), "text/html", "utf-8", null);
                        swipeRefreshLayout.setRefreshing(false);
                    },
                    err -> {
                        wv.setVisibility(View.INVISIBLE);
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText("Hubo un error al ejecutar la consulta dada.\nEl servidor respondió con mensaje:\n" + err);
                        Log.e(getClass().getSimpleName(), err);
                        swipeRefreshLayout.setRefreshing(false);
                    });
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (bus.isRegistered(this)) {
            bus.unregister(this);
        }
    }
}
