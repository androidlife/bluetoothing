package com.wordpress.laaptu.bluetooth.test.refactor.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.wordpress.laaptu.bluetooth.test.refactor.base.ConnectionMonitor;

import java.lang.ref.WeakReference;

import timber.log.Timber;


/**
 * This is just a wrapper of broad cast receivers.
 * This class can have object of type,
 * a. listening to bluetooth device off
 * b. listening to bluetooth connected two device
 * anyone, connection is closed
 * Normally in any of these cases, it is better
 * to close the activity/fragment or find ways
 * to power on the bluetooth.
 */

public class BluetoothConnectionMonitor implements ConnectionMonitor {

    public static final int LISTEN_FOR_BLUETOOTH_DEVICE = 0x1,
            LISTEN_FOR_BLUETOOTH_CONNECTION = 0x2;

    private WeakReference<Activity> activityWeakReference;
    private OnConnectionListener onConnectionListener;
    private int listenType = LISTEN_FOR_BLUETOOTH_DEVICE;

    public BluetoothConnectionMonitor(Activity activity, OnConnectionListener onConnectionListener, int listenType) {
        if (activity == null || onConnectionListener == null)
            throw new RuntimeException("Activity and OnConnectionListener must be valid");
        activityWeakReference = new WeakReference<>(activity);
        setOnConnectionListener(onConnectionListener);
        this.listenType = (listenType != LISTEN_FOR_BLUETOOTH_CONNECTION &&
                listenType != LISTEN_FOR_BLUETOOTH_DEVICE) ? this.listenType : listenType;
    }


    @Override
    public void start() {
        if (BluetoothAdapter.getDefaultAdapter() == null || !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            notifyConnectionLost();
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        switch (listenType) {
            case LISTEN_FOR_BLUETOOTH_CONNECTION:
                intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
                break;
            case LISTEN_FOR_BLUETOOTH_DEVICE:
                intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                break;
            default:
                break;
        }
        activityWeakReference.get().registerReceiver(bluetoothStateBR, intentFilter);
    }

    @Override
    public void stop() {
        if (activityWeakReference != null) {
            if (activityWeakReference.get() != null) {
                try {
                    activityWeakReference.get().unregisterReceiver(bluetoothStateBR);
                } catch (Exception e) {

                }
            }
            activityWeakReference.clear();
            activityWeakReference = null;
        }
        setOnConnectionListener(null);
    }

    @Override
    public void setOnConnectionListener(OnConnectionListener onConnectionListener) {
        this.onConnectionListener = onConnectionListener;
    }

    private void notifyConnectionLost() {
        if (onConnectionListener != null)
            onConnectionListener.connectionLost();
    }

    private void onBluetoothStateChanged(Intent intent) {
        int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
        switch (bluetoothState) {
            case BluetoothAdapter.STATE_ON:
                break;
            case BluetoothAdapter.ERROR:
            case BluetoothAdapter.STATE_OFF:
                Timber.d("Bluetooth device may be turned off");
                notifyConnectionLost();
                break;
            default:
                break;
        }
    }

    private final BroadcastReceiver bluetoothStateBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                onBluetoothStateChanged(intent);
            } else if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
                    || intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                Timber.d("Bluetooth peer connection is lost");
                notifyConnectionLost();
            }

        }
    };
}
