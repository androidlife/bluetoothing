package com.wordpress.laaptu.bluetooth.test.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import timber.log.Timber;

/**
 */

public class BluetoothConnectionMonitor extends BroadcastReceiver implements ConnectionMonitor {
    private OnConnectionLostListener listener;
    private Activity activity;
    private IntentFilter intentFilter;


    @Override
    public void start(Activity activity, OnConnectionLostListener listener) {
        this.listener = listener;
        intentFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        activity.registerReceiver(this, intentFilter);

    }

    @Override
    public void stop() {
        listener = null;
        try {
            activity.unregisterReceiver(this);
        } catch (Exception e) {

        }
        activity = null;
        intentFilter = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED) || intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)) {
            Timber.d("Device is disconnected");
            //BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (listener != null)
                listener.onConnectionLost();
        }

    }
}
