package com.sqless.sqlessmobile.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.sqless.sqlessmobile.R;

public class UIUtils {

    /**
     * Ejecuta el {@link Runnable} dado en el Thread que se encarga de renderizar la UI.
     *
     * @param runnable el Runnable a ejecutar.
     */
    public static void invokeOnUIThread(Runnable runnable) {
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

    public static void showInputDialog(Context context, String title, Callback<String> callbackYes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        View viewInflated = LayoutInflater.from(context).inflate(R.layout.input_dialog, ((Activity) context).findViewById(android.R.id.content), false);
        EditText txtInput = viewInflated.findViewById(R.id.txt_input);

        builder.setPositiveButton("OK", (dialog, which) -> callbackYes.exec(txtInput.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.setView(viewInflated);
        builder.show();
    }
}
