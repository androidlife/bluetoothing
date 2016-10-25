package com.wordpress.laaptu.bluetooth.test.connect;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wordpress.laaptu.bluetooth.R;
import com.wordpress.laaptu.bluetooth.test.base.PeerDiscoveryProvider;
import com.wordpress.laaptu.bluetooth.test.bluetooth.BluetoothProvider;

import timber.log.Timber;

import static android.R.style.Theme_Holo_Light_Panel;

public class ConnectActivity extends AppCompatActivity implements PeerDiscoveryProvider.NetworkDeviceListener {

    private OnlineFragment fragment;
    private PeerDiscoveryProvider peerDiscoveryProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        addOnlineFragment();
    }

    private void addOnlineFragment() {
        fragment = new OnlineFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.d("Paused");
        if (peerDiscoveryProvider != null) {
            peerDiscoveryProvider.stop();
            peerDiscoveryProvider = null;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("Resumed()");
        peerDiscoveryProvider = new BluetoothProvider(this);
        fragment.setProvider(peerDiscoveryProvider);
        peerDiscoveryProvider.start(this);
    }


    @Override
    public void onNetworkDeviceLost() {
        Timber.e("Network device connection error. Maybe network device is powered off");
        this.finish();
    }


    //--------UNNECESSARY PORTIONS
    private void addDialogFragment() {
        DialogFragment dialogFragment = new SomeDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "SomeDialog");
    }

    public static class SomeDialogFragment extends DialogFragment {
        public SomeDialogFragment() {
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), Theme_Holo_Light_Panel);
            builder.setTitle("Hello there");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return builder.create();
        }
    }

}
