package com.sqless.sqlessmobile.ui.fragments;

import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.utils.SQLUtils;

public class TableHtmlFragment extends AbstractFragment {


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
        WebView wv = fragmentView.findViewById(R.id.wv_table);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setDisplayZoomControls(false);
        wv.getSettings().setDefaultTextEncodingName("utf-8");

        SQLUtils.createHTMLFromQueryResult(connectionData, sql,
                doc -> wv.loadDataWithBaseURL(doc.getAssetsFolder(), doc.getHTML(), "text/html", "utf-8", null),
                err -> Log.e(getClass().getSimpleName(), err));
    }


    @Override
    protected void implementListeners(View containerView) {

    }

}
