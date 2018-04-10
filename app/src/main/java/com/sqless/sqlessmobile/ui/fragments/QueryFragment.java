package com.sqless.sqlessmobile.ui.fragments;

import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.utils.PrettifyHighlighter;

import java.lang.ref.WeakReference;

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
        HighlightSQLSyntaxTask syntaxTask = new HighlightSQLSyntaxTask(this);
        syntaxTask.execute(getArguments().getString("QUERY"));
    }

    @Override
    protected void implementListeners(View containerView) {

    }

    static class HighlightSQLSyntaxTask extends AsyncTask<String, Void, String> {
        WeakReference<QueryFragment> weakFragment;

        public HighlightSQLSyntaxTask(QueryFragment fragment) {
            this.weakFragment = new WeakReference<>(fragment);
        }

        @Override
        protected String doInBackground(String... sql) {
            PrettifyHighlighter highlighter = new PrettifyHighlighter();
            return highlighter.highlight("sql", sql[0]);
        }

        @Override
        protected void onPostExecute(String highlightedString) {
            ((TextView) weakFragment.get().fragmentView.findViewById(R.id.tv_query_sql)).setText(Html.fromHtml(highlightedString));
        }
    }
}
