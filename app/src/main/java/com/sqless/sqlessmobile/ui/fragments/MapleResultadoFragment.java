package com.sqless.sqlessmobile.ui.fragments;

import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;

import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.ui.busevents.maplequery.RunMapleEvent;
import com.sqless.sqlessmobile.utils.HTMLDoc;
import com.sqless.sqlessmobile.utils.PrettifyHighlighter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;
import java.util.List;

public class MapleResultadoFragment extends AbstractFragment {

    EventBus bus = EventBus.getDefault();
    private Spinner spinnerResults;
    private ArrayAdapter<String> spinnerAdapter;
    private List<HTMLDoc> htmlResults;
    private String sqlFromMaple;
    private String sqlExceptionFromMaple;
    private boolean mapleError;

    public MapleResultadoFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getTitle() {
        return "Maple";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_maple_resultado;
    }

    @Override
    public void afterCreate() {
        if (!bus.isRegistered(this)) {
            bus.register(this);
        }
        if (htmlResults != null) {
            prepareUI();
        } else if (sqlExceptionFromMaple != null) {
            prepareUIForError(sqlExceptionFromMaple, mapleError);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (bus.isRegistered(this)) {
            bus.unregister(this);
        }
    }

    @Subscribe
    public void onRunMapleHTMLReadyEvent(RunMapleEvent.HTMLReadyEvent event) {
        this.htmlResults = event.htmlResults;
        prepareUI();
    }

    @Subscribe
    public void onRunMapleSQLReadyEvent(RunMapleEvent.SQLReadyEvent event) {
        this.sqlFromMaple = event.sqlFromMaple;
    }

    @Subscribe
    public void onRunMapleSQLExceptionEvent(RunMapleEvent.SQLExceptionEvent event) {
        prepareUIForError(event.message, false);
    }

    @Subscribe
    public void onRunMapleMapleExceptionEvent(RunMapleEvent.MapleExceptionEvent event) {
        prepareUIForError(event.message, true);
    }

    @Subscribe
    public void onResultRequestEvent(RunMapleEvent.ResultRequestEvent event) {
        bus.post(new RunMapleEvent.ResultResponseEvent(htmlResults));
    }

    public void prepareUIForError(String errorMessage, boolean mapleError) {
        htmlResults = null;
        this.mapleError = mapleError;
        this.sqlExceptionFromMaple = errorMessage;
        TextView tvMapleErrors = fragmentView.findViewById(R.id.tv_maple_errors);
        tvMapleErrors.setText(sqlExceptionFromMaple);
        tvMapleErrors.setVisibility(View.VISIBLE);
        fragmentView.findViewById(R.id.webview_resultado).setVisibility(View.INVISIBLE);
        fragmentView.findViewById(R.id.spinner_resultado).setVisibility(View.INVISIBLE);
        fragmentView.findViewById(R.id.tv_sql_no_result).setVisibility(View.INVISIBLE);
        fragmentView.findViewById(R.id.btn_show_sql).setVisibility(mapleError ? View.INVISIBLE : View.VISIBLE);
    }

    public void prepareUI() {
        sqlExceptionFromMaple = null;
        String[] resultados = new String[htmlResults.size()];
        fragmentView.findViewById(R.id.tv_maple_errors).setVisibility(View.INVISIBLE);
        fragmentView.findViewById(R.id.btn_show_sql).setVisibility(View.VISIBLE);
        fragmentView.findViewById(R.id.webview_resultado).setVisibility(htmlResults.isEmpty() ? View.INVISIBLE : View.VISIBLE);
        fragmentView.findViewById(R.id.spinner_resultado).setVisibility(htmlResults.isEmpty() ? View.INVISIBLE : View.VISIBLE);
        fragmentView.findViewById(R.id.tv_sql_no_result).setVisibility(htmlResults.isEmpty() ? View.VISIBLE : View.INVISIBLE);

        if (!htmlResults.isEmpty()) {
            for (int i = 0; i < resultados.length; i++) {
                resultados[i] = "Resultado " + (i + 1);
            }
            spinnerResults = fragmentView.findViewById(R.id.spinner_resultado);
            spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, resultados);
            spinnerResults.setAdapter(spinnerAdapter);
        }
    }

    public void loadIntoWebView(int index) {
        WebView wv = fragmentView.findViewById(R.id.webview_resultado);
        if (!htmlResults.isEmpty()) {
            HTMLDoc doc = htmlResults.get(index);
            wv.loadDataWithBaseURL(doc.getAssetsFolder(), doc.getHTML(), "text/html", "utf-8", null);
        }
    }

    @Override
    protected void implementListeners(View containerView) {
        ((Spinner) containerView.findViewById(R.id.spinner_resultado)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadIntoWebView(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        containerView.findViewById(R.id.btn_show_sql).setOnClickListener(v -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_sql_from_maple, null);
            GenerateSQLSyntaxTask sqlSyntaxTask = new GenerateSQLSyntaxTask(dialogView);
            sqlSyntaxTask.execute(sqlFromMaple);
            dialogBuilder.setPositiveButton("OK", (dialog, which) -> activeDialog.dismiss());
            dialogBuilder.setTitle("SQL");
            dialogBuilder.setView(dialogView);
            activeDialog = dialogBuilder.show();
        });
    }

    static class GenerateSQLSyntaxTask extends AsyncTask<String, Void, String> {
        WeakReference<View> inflatedView;

        public GenerateSQLSyntaxTask(View inflatedView) {
            this.inflatedView = new WeakReference<>(inflatedView);
        }

        @Override
        protected String doInBackground(String... sql) {
            if (sql[0] != null) {
                PrettifyHighlighter highlighter = new PrettifyHighlighter();
                return highlighter.highlight("sql", sql[0]);
            }
            return "";
        }

        @Override
        protected void onPostExecute(String highlightedString) {
            ((TextView) inflatedView.get().findViewById(R.id.tv_sql_maple)).setText(Html.fromHtml(highlightedString));
        }
    }
}
