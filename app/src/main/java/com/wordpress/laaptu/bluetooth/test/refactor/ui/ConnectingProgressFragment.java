package com.wordpress.laaptu.bluetooth.test.refactor.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wordpress.laaptu.bluetooth.R;

import timber.log.Timber;


public class ConnectingProgressFragment extends DialogFragment {

    protected static final String TAG = "ConnectingProgressFragment";
    private String action, message;
    private int backgroundid;
    private int peerImage;
    private String peerName;
    private static final String ACTION = "action",
            BG_ID = "backgroundId", MSG = "message",
            PEER_NAME = "peerName", PEER_IMAGE = "peerImage",
            DIALOG_METHOD = "dialogMethod";
    private boolean isCancelled = false;
    private RequestDialog.DialogMethod dialogMethod;


    public static DialogFragment create(final String action, final int backgroundid,
                                        final String message,
                                        final String peerName, int peerImage, RequestDialog.DialogMethod dialogMethod) {
        ConnectingProgressFragment fragment = new ConnectingProgressFragment();
        Bundle params = new Bundle();
        params.putString(ACTION, action);
        params.putInt(BG_ID, backgroundid);
        params.putString(MSG, message);
        params.putString(PEER_NAME, peerName);
        params.putInt(PEER_IMAGE, peerImage);
        params.putParcelable(DIALOG_METHOD, dialogMethod);
        fragment.setArguments(params);
        return fragment;
    }

    private int style;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        initArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), style);
        builder.setView(createView(getActivity().getLayoutInflater()));
        Dialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Timber.d("Dialog is cancelled");
                if (dialogMethod != null)
                    dialogMethod.acceptReject(isCancelled);

            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Timber.d("Dialog is cancelled");
                isCancelled = true;
            }
        });

        return dialog;
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    private void initArguments() {
        Bundle params = getArguments();
        action = params.getString(ACTION);
        backgroundid = params.getInt(BG_ID);
        peerImage = params.getInt(PEER_IMAGE);
        peerName = params.getString(PEER_NAME);
        boolean isDark = "TouchDice".equals(action) || "TouchTrails".equals(action);
        style = isDark ? android.R.style.Theme_Holo_NoActionBar_Fullscreen : android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen;
        //setStyle(DialogFragment.STYLE_NO_FRAME, style);
        dialogMethod = params.getParcelable(DIALOG_METHOD);
    }

    public View createView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.fragment_connecting, null);
        if (backgroundid != -1) {
            //TODO resize image
            //view.setBackgroundResource(backgroundid);
        } else {
            //view.setBackgroundDrawable(null);
        }
        TextView title = (TextView) view.findViewById(R.id.connectingTitle);
        if ("TouchChat".equals(action)) {
            title.setVisibility(View.INVISIBLE);
        } else {
            title.setText(message);
            title.setVisibility(View.VISIBLE);
        }

        TextView stop = (TextView) view.findViewById(R.id.stopButton);
        if ("TouchTrails".equals(action)) {
            stop.setTextColor(Color.rgb(169, 120, 52));
        } else if ("TouchVideo".equals(action)) {
            stop.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
        } else if ("TouchDice".equals(action)) {
            stop.setTextColor(Color.rgb(169, 120, 52));
        } else if ("TouchChat".equals(action)) {
            stop.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
        }
        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //TODO: pass the info about cancellation
                //getFragmentManager().popBackStack();
                isCancelled = true;
                dismiss();
                dialogMethod.acceptReject(false);
            }
        });
        ImageView peerPic = (ImageView) view.findViewById(R.id.userPic2);
        if (peerPic != null) {
            //TODO add pic
            //peerPic.setImageResource(peerImage);
        } else {
            //Log.e(TAG, "Couldn't find user pic");
        }

        TextView peerNameTxt = (TextView) view.findViewById(R.id.userNameView2);
        peerNameTxt.setText(peerName);

        ProgressBar progress = (ProgressBar) view.findViewById(R.id.progressBar1);
        progress.animate();

        return view;
    }


}
