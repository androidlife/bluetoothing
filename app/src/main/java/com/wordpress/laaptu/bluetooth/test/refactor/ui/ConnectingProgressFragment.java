package com.wordpress.laaptu.bluetooth.test.refactor.ui;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private RequestDialog.DialogMethod dialogMethod;
    private static final String ACTION = "action",
            BG_ID = "backgroundId", MSG = "message", PEER_NAME = "peerName",
            PEER_IMAGE = "peerImage", DIALOG_METHOD = "dialogMethod";
    private boolean normalDismiss = true;


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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isDark = "TouchDice".equals(action) || "TouchTrails".equals(action);
        int style = isDark ? android.R.style.Theme_Holo_NoActionBar_Fullscreen : android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen;
        setStyle(DialogFragment.STYLE_NO_FRAME, style);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (dialogMethod != null) {
            dialogMethod.acceptReject(normalDismiss);
            dialogMethod = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                Timber.d("On back bressed =%b", keyCode == KeyEvent.KEYCODE_BACK);
                if (keyCode == KeyEvent.KEYCODE_BACK && dialogMethod != null) {
                    normalDismiss = false;
                    dismiss();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Bundle params = getArguments();
        action = params.getString(ACTION);
        backgroundid = params.getInt(BG_ID);
        peerImage = params.getInt(PEER_IMAGE);
        peerName = params.getString(PEER_NAME);
        dialogMethod = params.getParcelable(DIALOG_METHOD);


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
                normalDismiss = false;
                dismiss();

            }
        });
        ImageView peerPic = (ImageView) view.findViewById(R.id.userPic2);
        if (peerPic != null) {
            //TODO add pic
            //peerPic.setImageResource(peerImage);
        } else {
            Log.e(TAG, "Couldn't find user pic");
        }

        TextView peerNameTxt = (TextView) view.findViewById(R.id.userNameView2);
        peerNameTxt.setText(peerName);

        ProgressBar progress = (ProgressBar) view.findViewById(R.id.progressBar1);
        progress.animate();


        return view;
    }


}
