package com.wordpress.laaptu.bluetooth.test.refactor.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wordpress.laaptu.bluetooth.R;


public class ConnectingProgressFragment extends DialogFragment {

    protected static final String TAG = "ConnectingProgressFragment";
    private String action, message;
    private int backgroundid;
    private int peerImage;
    private String peerName;
    private static final String ACTION = "action", BG_ID = "backgroundId", MSG = "message", PEER_NAME = "peerName", PEER_IMAGE = "peerImage";


    public static DialogFragment create(final String action, final int backgroundid,
                                        final String message,
                                        final String peerName, int peerImage) {
        ConnectingProgressFragment fragment = new ConnectingProgressFragment();
        Bundle params = new Bundle();
        params.putString(ACTION, action);
        params.putInt(BG_ID, backgroundid);
        params.putString(MSG, message);
        params.putString(PEER_NAME, peerName);
        params.putInt(PEER_IMAGE, peerImage);
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Bundle params = getArguments();
        action = params.getString(ACTION);
        backgroundid = params.getInt(BG_ID);
        peerImage = params.getInt(PEER_IMAGE);
        peerName = params.getString(PEER_NAME);


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
