package com.sqless.sqlessmobile.network;

import android.util.Log;

import com.sqless.sqlessmobile.utils.UIUtils;

import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

public abstract class RestRequest {

    protected String url;
    protected Resty rest;
    protected boolean newThread = true;

    public RestRequest(String url) {
        this.rest = new Resty(Resty.Option.timeout(5000));
        this.url = url;
    }

    public RestRequest(String url, boolean newThread) {
        this(url);
        this.newThread = newThread;
    }

    /**
     * Es llamado por {@link #executePostExec(us.monoid.json.JSONObject)} al
     * completarse el request contra el servidor sin errores.
     *
     * @param json
     * @throws Exception si hubo algún error dentro de este método. Por ejemplo,
     *                   si el usuario manipula el JSON de manera erronea. Todas las excepciones
     *                   de este método serán atrapadas por {@link #onFailure(java.lang.String)}
     */
    public void onSuccess(JSONObject json) throws Exception {
    }

    /**
     * Es llamado luego de un request, haya sido erroneo o exitoso. Este método se ejecutará
     * en el UI thread.
     */
    public void afterRequest() {
    }

    /**
     * Si hubo alguna excepción al mandar el request contra el servidor o si el
     * usuario manipuló de forma erronea el JSON del request.
     *
     * @param message El mensaje de error.
     */
    public void onFailure(String message) {
        Log.e("RestRequest", "Request error " + message);
    }

    /**
     * Cada request se ejecuta de distinta forma, pero las tareas post-ejecución
     * siempre son iguales, es por eso que toda implementación de
     * {@link #exec()} debe llamar a este método y pasarle el JSON resultante.
     * Es en este método en donde se ejecutarán los callback dados dependiendo
     * el resultado de la ejecución.
     *
     * @param json El JSON resultante que el servidor devolvió al ser ejecutado
     *             el request.
     * @see #onSuccess(us.monoid.json.JSONObject)
     * @see #onFailure(java.lang.String)
     */
    protected final void executePostExec(JSONObject json) {
        Runnable runnable = () -> {
            try {
                onSuccess(json);
//                int tokenStatus = Integer.parseInt(json.get("user_id").toString());
//                if (tokenStatus != -1) {
//                    onTokenSuccess(json);
//                }
            } catch (Exception e) {
                onFailure(e.getMessage());
            }
        };
        if (newThread) {
            UIUtils.invokeOnUIThread(runnable);
        } else {
            runnable.run();
        }
    }

    /**
     * Ejecuta este HTTP request contra el servidor. Para el correcto
     * funcionamiento de este método, es indispensable que toda implementación
     * de éste incluya una llamada a
     * {@link #executePostExec(us.monoid.json.JSONObject)}.
     */
    public abstract void exec();
}
