package com.wordpress.laaptu.bluetooth.test.base;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;

/**
 */

public class BluetoothDeviceActivator implements NetworkDeviceActivator {

    private Activity activity;
    private NetworkDeviceActivationListener listener;
    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_BLUETOOTH_ENABLE = 0x9;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BLUETOOTH_ENABLE && listener != null) {
            if (resultCode == RESULT_OK || resultCode == RESULT_FIRST_USER) {
                Timber.d("Bluetooth access given by user");
                setDeviceActivated(true);
            } else {
                Timber.e("Bluetooth access not given by user");
                setDeviceActivated(false);
            }
        }
    }

    @Override
    public void checkDeviceActivation(Context context, NetworkDeviceActivationListener listener) {
        if (context == null || !(context instanceof Activity) || listener == null)
            throw new
                    RuntimeException("BluetoothDeviceActivator only works with valid instance of Activity and non null NetworkDeviceActivationListener");
        activity = (Activity) context;
        this.listener = listener;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        checkIfBluetoothIsActivated();
    }

    private void checkIfBluetoothIsActivated() {
        if (bluetoothAdapter == null) {
            Timber.e("This device doesn't have a bluetooth hardware");
            setDeviceActivated(false);
            return;
        }
        if (bluetoothAdapter.isEnabled()) {
            Timber.d("Bluetooth is activated.");
            setDeviceActivated(true);
            return;
        }
        Timber.d("Bluetooth is powered off,starting intent for powering on bluetooth");
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        activity.startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
    }

    private void setDeviceActivated(boolean isActivated) {
        if (isActivated)
            listener.onDeviceActivated();
        else
            listener.onDeviceDeactivated();
        release();
    }

    @Override
    public void release() {
        activity = null;
        listener = null;
        bluetoothAdapter = null;
    }
}
