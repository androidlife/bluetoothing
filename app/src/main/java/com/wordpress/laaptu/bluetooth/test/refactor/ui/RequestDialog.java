package com.wordpress.laaptu.bluetooth.test.refactor.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;

/**
 */

public class RequestDialog extends DialogFragment {
    public interface DialogMethodInterface extends Parcelable {
        void acceptReject(boolean accept);
    }

    public static class DialogMethod implements DialogMethodInterface {
        @Override
        public void acceptReject(boolean accept) {
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }

    private static final String TITLE = "title", MESSAGE = "msg",
            DIALOG_STYLE = "dialogStyle", DIALOG_METHOD = "dialogMethod", SHOW_NEUTRAL = "showNeutral";
    private DialogMethod dialogMethod;

    public RequestDialog() {

    }

    public static RequestDialog getInstance(String title, String message, int dialogStyle, boolean showNeutral, DialogMethod dialogMethod) {
        RequestDialog fragment = new RequestDialog();
        Bundle params = new Bundle();
        params.putString(TITLE, title);
        params.putString(MESSAGE, message);
        params.putInt(DIALOG_STYLE, dialogStyle);
        params.putParcelable(DIALOG_METHOD, dialogMethod);
        params.putBoolean(SHOW_NEUTRAL, showNeutral);
        fragment.setArguments(params);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle params = getArguments();
        if (params == null) {
            return null;
        }
        int dialogStyle = params.getInt(DIALOG_STYLE);
        String message = params.getString(MESSAGE);
        String title = params.getString(TITLE);
        dialogMethod = params.getParcelable(DIALOG_METHOD);
        boolean showNetural = params.getBoolean(SHOW_NEUTRAL);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), dialogStyle);
        if (!TextUtils.isEmpty(title))
            builder.setTitle(title);
        if (!TextUtils.isEmpty(message))
            builder.setMessage(message);

        if (showNetural) {
            builder.setNeutralButton("Okay", null);
        } else {
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    acceptReject(true, dialog);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    acceptReject(false, dialog);
                }
            });
        }

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private void acceptReject(final boolean accept, final DialogInterface dialog) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                if (dialogMethod != null) {
                    dialogMethod.acceptReject(accept);
                    dialogMethod = null;
                }
            }
        });
    }
}
