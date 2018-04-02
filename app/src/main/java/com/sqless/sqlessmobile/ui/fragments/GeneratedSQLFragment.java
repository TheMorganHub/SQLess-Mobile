package com.sqless.sqlessmobile.ui.fragments;


import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.busevents.createtable.MustGenerateSQLEvent;
import com.sqless.sqlessmobile.utils.PrettifyHighlighter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;

public class GeneratedSQLFragment extends AbstractFragment {

    EventBus bus = EventBus.getDefault();

    public GeneratedSQLFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getTitle() {
        return "Crear tabla";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_generated_sql;
    }

    @Override
    public void afterCreate() {
        if (!bus.isRegistered(this)) {
            bus.register(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (bus.isRegistered(this)) {
            bus.unregister(this);
        }
    }

    @Override
    protected void implementListeners(View containerView) {
    }

    @Subscribe
    public void onMustGenerateSQLEvent(MustGenerateSQLEvent event) {
        GenerateSQLSyntaxTask generateSQLSyntaxTask = new GenerateSQLSyntaxTask(this);
        generateSQLSyntaxTask.execute(event);
    }

    static class GenerateSQLSyntaxTask extends AsyncTask<MustGenerateSQLEvent, Void, String> {
        WeakReference<GeneratedSQLFragment> weakFragment;

        public GenerateSQLSyntaxTask(GeneratedSQLFragment fragment) {
            this.weakFragment = new WeakReference<>(fragment);
        }

        @Override
        protected void onPreExecute() {
            weakFragment.get().fragmentView.findViewById(R.id.progress_bar_generatesql).setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(MustGenerateSQLEvent... event) {
            PrettifyHighlighter highlighter = new PrettifyHighlighter();
            return highlighter.highlight("sql", event[0].table.generateCreateStatement());
        }

        @Override
        protected void onPostExecute(String highlightedString) {
            ((TextView) weakFragment.get().fragmentView.findViewById(R.id.tv_generated_sql)).setText(Html.fromHtml(highlightedString));
            weakFragment.get().fragmentView.findViewById(R.id.progress_bar_generatesql).setVisibility(View.INVISIBLE);
        }
    }

}
