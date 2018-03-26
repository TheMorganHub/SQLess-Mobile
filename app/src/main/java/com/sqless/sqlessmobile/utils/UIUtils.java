package com.sqless.sqlessmobile.utils;

import android.os.Handler;
import android.os.Looper;

public class UIUtils {

    /**
     * Ejecuta el {@link Runnable} dado en el Thread que se encarga de renderizar la UI.
     *
     * @param runnable el Runnable a ejecutar.
     */
    public static void invokeOnUI(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
