package com.sqless.sqlessmobile.ui.fragments;

import android.os.AsyncTask;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.network.GoogleTokenManager;
import com.sqless.sqlessmobile.network.PostRequest;
import com.sqless.sqlessmobile.network.RestRequest;
import com.sqless.sqlessmobile.ui.busevents.maplequery.MapleExecutionReadyEvent;
import com.sqless.sqlessmobile.ui.busevents.maplequery.RunMapleEvent;
import com.sqless.sqlessmobile.utils.HTMLDoc;
import com.sqless.sqlessmobile.utils.SQLUtils;
import com.sqless.sqlessmobile.utils.TextUtils;
import com.sqless.sqlessmobile.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

public class MapleCrearFragment extends AbstractFragment {

    private WebView editorWebView;
    EventBus bus = EventBus.getDefault();
    private JSInterface jsInterface;

    public MapleCrearFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getTitle() {
        return "Maple";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_maple_crear;
    }


    @Override
    public void afterCreate() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        WebView editorWebView = fragmentView.findViewById(R.id.wv_editor);
        if (jsInterface == null) {
            jsInterface = new JSInterface();
        }
        editorWebView.addJavascriptInterface(jsInterface, "jsInterface");
        loadHTMLEditor();
    }

    public void loadHTMLEditor() {
        editorWebView = fragmentView.findViewById(R.id.wv_editor);
        editorWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                editorWebView.evaluateJavascript("setValue(\"" + jsInterface.getEditorContent(true) + "\");", null);
            }
        });
        editorWebView.getSettings().setJavaScriptEnabled(true);
        editorWebView.loadUrl("file:///android_asset/editorhtml/editor.html");
    }


    @Override
    public void onFabClicked() {
        if (fragmentView.findViewById(R.id.progress_bar_maple).getVisibility() == View.VISIBLE) { //ya hay una consulta cargando
            return;
        }
        GoogleTokenManager.getInstance().silentSignIn(getActivity(), account -> {
            WebView editorWebView = fragmentView.findViewById(R.id.wv_editor);
            editorWebView.evaluateJavascript("shareEditorContent();", n -> doExecuteMaple(account.getIdToken(), jsInterface.getEditorContent()));
        });
    }

    public void doExecuteMaple(String idToken, String mapleStatement) {
        fragmentView.findViewById(R.id.progress_bar_maple).setVisibility(View.VISIBLE);
        RestRequest mapleRequest = new PostRequest(getString(R.string.maple_url), Resty.data("maple_statement", mapleStatement), Resty.data("id_token", idToken)) {
            @Override
            public void onSuccess(JSONObject json) throws Exception {
                if (json.has("err")) { //hubo un error interno en el server
                    onFailure(json.getString("err"));
                    return;
                }
                String sql = json.getString("CONVERTED_SQL");
                RunMapleQuery mapleTask = new RunMapleQuery(MapleCrearFragment.this);
                mapleTask.execute(sql);
            }

            @Override
            public void onFailure(String message) {
                UIUtils.invokeOnUIThreadIfNotDestroyed(getActivity(), () -> {
                    bus.post(new RunMapleEvent.MapleExceptionEvent(message));
                    bus.post(new MapleExecutionReadyEvent());
                });
            }

            @Override
            public void afterRequest() {
                fragmentView.findViewById(R.id.progress_bar_maple).setVisibility(View.INVISIBLE);
            }
        };
        mapleRequest.exec();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        editorWebView.evaluateJavascript("persistData();", n -> {
        });
    }

    @Override
    protected void implementListeners(View containerView) {
    }

    private class JSInterface {

        private String data;

        public JSInterface() {
            this.data = "";
        }

        @JavascriptInterface
        public void setData(String data) {
            this.data = data;
        }

        public String getEditorContent() {
            return getEditorContent(false);
        }

        public String getEditorContent(boolean unescapeContent) {
            return !unescapeContent ? data : TextUtils.unEscapeString(data);
        }
    }

    static class RunMapleQuery extends AsyncTask<String, String, List<HTMLDoc>> {
        WeakReference<MapleCrearFragment> fragment;
        String sql;
        boolean queryStatus;
        String exceptionMessage;

        public RunMapleQuery(MapleCrearFragment fragment) {
            this.fragment = new WeakReference<>(fragment);
        }

        @Override
        protected List<HTMLDoc> doInBackground(String... strings) {
            List<HTMLDoc> htmlResults = new ArrayList<>();
            try {
                Connection conn = fragment.get().connectionData.makeConnection(fragment.get().getActivity());
                Statement statement = conn.createStatement();
                int updateCount = 0;
                this.sql = strings[0];
                boolean hasResult = statement.execute(strings[0]);
                while (hasResult || (updateCount = statement.getUpdateCount()) != -1) {
                    if (hasResult) {
                        htmlResults.add(SQLUtils.createHTMLFromResultSet(statement.getResultSet()));
                    }
                    hasResult = statement.getMoreResults();
                }
                queryStatus = true;
            } catch (SQLException ex) {
                queryStatus = false;
                exceptionMessage = "Hubo un error al ejecutar la sentencia SQL dada.\nEl servidor MySQL respondió con mensaje:\n" + ex.getMessage();
            }
            return htmlResults;
        }

        @Override
        protected void onPostExecute(List<HTMLDoc> s) {
            MapleCrearFragment frag = fragment.get();
            UIUtils.hideKeyboardAt(frag.getActivity());
            if (queryStatus) {
                frag.bus.post(new RunMapleEvent.HTMLReadyEvent(s));
            } else {
                frag.bus.post(new RunMapleEvent.SQLExceptionEvent(exceptionMessage));
            }
            frag.bus.post(new RunMapleEvent.SQLReadyEvent(sql));
            frag.bus.post(new MapleExecutionReadyEvent());
        }
    }
}
