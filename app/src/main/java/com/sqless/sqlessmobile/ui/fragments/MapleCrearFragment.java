package com.sqless.sqlessmobile.ui.fragments;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;

import android.webkit.WebViewClient;
import com.sqless.sqlessmobile.R;
import com.sqless.sqlessmobile.network.GoogleTokenManager;
import com.sqless.sqlessmobile.network.PostRequest;
import com.sqless.sqlessmobile.network.RestRequest;
import com.sqless.sqlessmobile.utils.TextUtils;
import org.apache.commons.text.StringEscapeUtils;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

public class MapleCrearFragment extends AbstractFragment {

    private WebView editorWebView;
    private String value = "";


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
        loadHTMLEditor();
    }

    public void loadHTMLEditor() {
        editorWebView = fragmentView.findViewById(R.id.wv_editor);
        editorWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                editorWebView.evaluateJavascript("setValue(\"" + TextUtils.unEscapeString(value) + "\");", null);
            }
        });
        editorWebView.getSettings().setJavaScriptEnabled(true);
        editorWebView.loadUrl("file:///android_asset/editorhtml/editor.html");
    }



    @Override
    public void onFabClicked() {
        GoogleTokenManager.getInstance().silentSignIn(getActivity(), account -> {
            WebView editorWebView = fragmentView.findViewById(R.id.wv_editor);
            editorWebView.evaluateJavascript("getValue();", value -> {
                String mapleStatement = StringEscapeUtils.unescapeJava(value).replaceAll("\"", "");
                doExecuteMaple(account.getIdToken(), mapleStatement);
            });
        });
    }

    public void doExecuteMaple(String idToken, String mapleStatement) {
        RestRequest mapleRequest = new PostRequest(getString(R.string.maple_url), Resty.form(Resty.data("maple_statement", mapleStatement),
                Resty.data("id_token", idToken))) {
            @Override
            public void onSuccess(JSONObject json) throws Exception {
                if (json.has("err")) { //hubo un error interno en el server
                    onFailure(json.getString("err"));
                    return;
                }
                Log.i("MapleCrearFragment", json.getString("CONVERTED_SQL"));
            }

            @Override
            public void onFailure(String message) {
                Log.i("MapleCrearFragment", message);
            }
        };
        mapleRequest.exec();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        editorWebView.evaluateJavascript("getValue();", value -> this.value = value.replaceAll("\"", ""));
    }

    @Override
    protected void implementListeners(View containerView) {
    }
}
