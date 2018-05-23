package com.sqless.sqlessmobile.ui.fragments;

import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.utils.SQLUtils;

public class TableHtmlFragment extends AbstractFragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeRefreshLayout;


    public TableHtmlFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getTitle() {
        return getArguments().getString("QUERY_TITLE");
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_table_html;
    }

    @Override
    public void afterCreate() {
        String queryContents = getArguments().getString("QUERY");
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
        SQLUtils.createHTMLFromQueryResult(connectionData, sql,
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

    @Override
    public void onRefresh() {
        String queryContents = getArguments().getString("QUERY");
        if (queryContents != null) {
            TextView tvError = fragmentView.findViewById(R.id.tv_table_html_error);
            WebView wv = fragmentView.findViewById(R.id.wv_table);
            SQLUtils.createHTMLFromQueryResult(connectionData, queryContents,
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
}
