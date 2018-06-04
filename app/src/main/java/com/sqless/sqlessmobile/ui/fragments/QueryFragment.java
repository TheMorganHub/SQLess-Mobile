package com.sqless.sqlessmobile.ui.fragments;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.utils.PrettifyHighlighter;

public class QueryFragment extends AbstractFragment {


    public QueryFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getTitle() {
        return getArguments().getString("QUERY_TITLE");
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_query;
    }

    @Override
    public void afterCreate() {
        String sql = getArguments().getString("query_to_run");
        PrettifyHighlighter highlighter = new PrettifyHighlighter();
        String highlighted = highlighter.highlight("sql", sql);
        ((TextView) fragmentView.findViewById(R.id.tv_query_sql)).setText(Html.fromHtml(highlighted));
    }

    @Override
    protected void implementListeners(View containerView) {

    }
}
