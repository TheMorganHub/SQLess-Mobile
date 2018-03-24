package com.sqless.sqlessmobile.network;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class PostRequest extends StringRequest {
    private Map<String, String> paramsMap;

    public PostRequest(String url, Response.Listener<String> listener, String params) {
        super(Method.POST, url, listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERR", error.getMessage());
            }
        });
        setParamsMap(params);
    }

    private void setParamsMap(String params) {
        paramsMap = new HashMap<>();
        if (params != null) {
            String[] paramsNoAmpersand = params.split("&");
            for (String param : paramsNoAmpersand) {
                String[] paramsNoEquals = param.split("=");
                paramsMap.put(paramsNoEquals[0], paramsNoEquals[1]);
            }
        }
        Log.i("PARAMS", paramsMap.toString());
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return paramsMap;
    }
}
