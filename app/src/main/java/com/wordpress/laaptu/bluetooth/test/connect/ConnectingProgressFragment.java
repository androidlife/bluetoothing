package com.wordpress.laaptu.bluetooth.test.connect;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wordpress.laaptu.bluetooth.R;
import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;


public class ConnectingProgressFragment extends Fragment {

    protected static final String TAG = "ConnectingProgressFragment";
    private static String action, message;
    private static int backgroundid;
    private static AsyncTask<Object, Void, Void> connectionTask;
    private static DiscoveredPeer peer;


    public static Fragment create(final String action, final int backgroundid, final AsyncTask<Object, Void, Void> connectionTask, final String message,
                                  final DiscoveredPeer peer) {

        ConnectingProgressFragment.action = action;
        ConnectingProgressFragment.backgroundid = backgroundid;
        ConnectingProgressFragment.connectionTask = connectionTask;
        ConnectingProgressFragment.backgroundid = backgroundid;
        ConnectingProgressFragment.peer = peer;
        return new ConnectingProgressFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        boolean isDark = "TouchDice".equals(action) || "TouchTrails".equals(action);
        int style = isDark ? android.R.style.Theme_Holo_NoActionBar_Fullscreen : android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen;
        View view = inflater.inflate(R.layout.fragment_connecting, null);
        if (backgroundid != -1) {
            view.setBackgroundResource(backgroundid);
        } else {
            view.setBackgroundDrawable(null);
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
                if (connectionTask != null) {
                    connectionTask.cancel(true);
                }
                getFragmentManager().popBackStack();
            }
        });
        ImageView peerPic = (ImageView) view.findViewById(R.id.userPic2);
        if (peerPic != null) {
            peerPic.setImageResource(peer.getPicture());
        } else {
            Log.e(TAG, "Couldn't find user pic");
        }

        TextView peerName = (TextView) view.findViewById(R.id.userNameView2);
        peerName.setText(peer.getName());

        ProgressBar progress = (ProgressBar) view.findViewById(R.id.progressBar1);
        progress.animate();

        return view;
    }


}
