package com.sqless.sqlessmobile.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.widget.Spinner;

public class UIUtils {

    /**
     * Ejecuta el {@link Runnable} dado en el Thread que se encarga de renderizar la UI.
     *
     * @param runnable el Runnable a ejecutar.
     */
    public static void invokeOnUI(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public static void selectSpinnerItemByValue(Spinner spinner, String val) {
        spinner.setSelection(getIndex(spinner, val));
    }

    private static int getIndex(Spinner spinner, String val) {
        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(val)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public static String getTagForFragment(Fragment fragment) {
        return fragment.getClass().getName();
    }
}
