package com.sqless.sqlessmobile.network;

import com.sqless.sqlessmobile.utils.UIUtils;

import us.monoid.json.JSONObject;
import us.monoid.web.mime.MultipartContent;

public class PostRequest extends RestRequest {

    private MultipartContent form;

    public PostRequest(String url, MultipartContent data) {
        super(url);
        form = data;
    }

    public PostRequest(String url, boolean newThread, MultipartContent data) {
        this(url, data);
        super.newThread = newThread;
    }

    @Override
    public void exec() {
        Runnable runnable = () -> {
            try {
                JSONObject json = rest.json(url, form).object();
                executePostExec(json);
            } catch (Exception e) {
                onFailure(e.getMessage());
            } finally {
                UIUtils.invokeOnUIThread(() -> afterRequest());
            }
        };
        if (newThread) {
            Thread networkThread = new Thread(runnable);
            networkThread.start();
        } else {
            runnable.run();
        }
    }
}
