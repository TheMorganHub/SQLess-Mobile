package com.sqless.sqlessmobile.ui.fragments;

import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.utils.TextUtils;

public class QueryFragment extends AbstractFragment {


    public QueryFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getTitle() {
        return getArguments().getString("query_title");
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_query;
    }

    @Override
    public void afterCreate() {
        String sql = getArguments().getString("query_to_run");
        WebView wvQuery = fragmentView.findViewById(R.id.wv_query_sql);
        wvQuery.getSettings().setJavaScriptEnabled(true);
        wvQuery.loadUrl("file:///android_asset/editorhtml/readonly_editor.html");
        wvQuery.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                wvQuery.evaluateJavascript("setValue(\"" + TextUtils.unEscapeString(sql) + "\");", null);
            }
        });
    }

    @Override
    protected void implementListeners(View containerView) {

    }
}
