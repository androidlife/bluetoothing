package com.wordpress.laaptu.bluetooth.test.refactor.ui;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
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
    private Picasso picasso;
    private int imageSize;
    private View view;


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
        picasso.cancelRequest(backgroundTarget);
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

    private Target backgroundTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if(view !=null){
                view.setBackground(new BitmapDrawable(getResources(),bitmap));
            }

        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            if(view !=null)
                view.setBackground(null);

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        picasso = Picasso.with(getActivity());
        imageSize = getResources().getDimensionPixelSize(R.dimen.connect_me_size);
        Bundle params = getArguments();
        action = params.getString(ACTION);
        backgroundid = params.getInt(BG_ID);
        peerImage = params.getInt(PEER_IMAGE);
        peerName = params.getString(PEER_NAME);
        dialogMethod = params.getParcelable(DIALOG_METHOD);


        view = inflater.inflate(R.layout.fragment_connecting, null);
        if (backgroundid != -1) {
            //TODO resize image
            //view.setBackgroundResource(backgroundid);
            picasso.load(backgroundid).resize(600, 600).into(backgroundTarget);
        } else {
            view.setBackground(null);
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
            picasso.load(peerImage).resize(imageSize, imageSize).into(peerPic);
        }

        TextView peerNameTxt = (TextView) view.findViewById(R.id.userNameView2);
        peerNameTxt.setText(peerName);

        ProgressBar progress = (ProgressBar) view.findViewById(R.id.progressBar1);
        progress.animate();


        return view;
    }


}
