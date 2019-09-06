package com.sqless.sqlessmobile.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;

import com.sqless.sqlessmobile.R;

public class UIUtils {

    /**
     * Ejecuta el callback dado en el Thread que se encarga de renderizar la UI.
     *
     * @param runnable el Runnable a ejecutar.
     */
    public static void invokeOnUIThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public static void invokeOnUIThreadIfNotDestroyed(Activity activity, Runnable runnable) {
        if (activity != null && !activity.isDestroyed()) {
            invokeOnUIThread(runnable);
        }
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

    public static void hideKeyboardAt(Activity context) {
        View view = context.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showInputDialog(Context context, String title, Callback<String> callbackYes) {
        showInputDialog(context, title, callbackYes, null);
    }

    public static void showInputDialog(Context context, String title, Callback<String> callbackYes, Runnable ifEmptyInputCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        View viewInflated = LayoutInflater.from(context).inflate(R.layout.input_dialog, ((Activity) context).findViewById(android.R.id.content), false);
        EditText txtInput = viewInflated.findViewById(R.id.txt_input);

        builder.setPositiveButton("OK", null)
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel())
                .setView(viewInflated);
        AlertDialog dialog = builder.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String input = txtInput.getText().toString();
            if (ifEmptyInputCallback != null && input.isEmpty()) {
                ifEmptyInputCallback.run();
            } else {
                dialog.dismiss();
                callbackYes.exec(txtInput.getText().toString());
            }
        });
    }

    public static void showConfirmationDialog(Activity context, String title, String msg, Runnable callbackYes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Yes", (dialog, which) -> UIUtils.invokeOnUIThreadIfNotDestroyed(context, callbackYes))
                .setNegativeButton("No", null)
                .show();
    }

    public static void showMessageDialogWithNeutralButton(Context context, String title, String msg, String neutralBtnText, Runnable callbackNeutral) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .setNeutralButton(neutralBtnText, (dialog, which) -> callbackNeutral.run())
                .show();
    }

    public static void showMessageDialog(Context context, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    public static void showMessageDialog(Context context, String title, String msg, Runnable callbackOk) {
        showMessageDialog(context, title, msg, callbackOk, null);
    }

    public static void showMessageDialog(Context context, String title, String msg, Runnable callbackOk, Runnable callbackDismiss) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", (dialog, which) -> callbackOk.run());
        AlertDialog alertDialog = builder.create();
        if (callbackDismiss != null) {
            alertDialog.setOnDismissListener(dialog -> callbackDismiss.run());
        }
        alertDialog.show();
    }

}
