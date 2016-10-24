package com.wordpress.laaptu.bluetooth.test.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;
import com.wordpress.laaptu.bluetooth.test.base.PeerDiscoveryProvider;

/**
 */

public class BluetoothProvider implements PeerDiscoveryProvider {

    private final Activity activity;
    private IntentFilter bluetoothStateIntentFilter;
    private NetworkDeviceListener networkDeviceListener;

    public BluetoothProvider(Activity activity) {
        this.activity = activity;
    }


    private final BroadcastReceiver bluetoothStateBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                switch (bluetoothState) {
                    case BluetoothAdapter.STATE_ON:
                        break;
                    case BluetoothAdapter.ERROR:
                    case BluetoothAdapter.STATE_OFF:
                        if (networkDeviceListener != null)
                            networkDeviceListener.onNetworkDeviceLost();
                        stop();
                        break;
                    default:
                        break;
                }
            }

        }
    };

    /**
     * PeerDiscoveryProvider methods
     * ....Starts
     */
    @Override
    public void start() {
        bluetoothStateIntentFilter = new IntentFilter();
        bluetoothStateIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        activity.registerReceiver(bluetoothStateBR, bluetoothStateIntentFilter);
    }

    @Override
    public void start(NetworkDeviceListener networkDeviceListener) {
        this.networkDeviceListener = networkDeviceListener;
        start();
    }

    @Override
    public void stop() {
        networkDeviceListener = null;
        try {
            activity.unregisterReceiver(bluetoothStateBR);
        } catch (Exception e) {

        }
    }

    @Override
    public void reload() {

    }

    @Override
    public void setIdentifier(String newIdentifier) {

    }

    @Override
    public void setOnPeerDiscoveredListener(OnPeerDiscoveredListener listener) {

    }

    @Override
    public void setAction(String action) {

    }


    /**
     * PeerDiscoveryProvider methods
     * ....Ends
     */

    private static class Peer implements DiscoveredPeer {

        private UserPool.User user;
        private BluetoothDevice bluetoothDevice;

        Peer(BluetoothDevice bluetoothDevice) {
            user = UserPool.getUserByName(bluetoothDevice.getAddress());
            this.bluetoothDevice = bluetoothDevice;
        }

        /**
         * DiscoveredPeer methods
         * ....Starts
         */

        @Override
        public String getName() {
            return user.name;
        }

        @Override
        public int getPicture() {
            return user.portraitid;
        }

        @Override
        public String getStatus() {
            return UserPool.User.STATUS_ONLINE;
        }

        @Override
        public void connectTo(ConnectionListener connectionListener) {

        }

        @Override
        public int getPriority() {
            return PRIORITY_HIGHEST;
        }

        @Override
        public void release() {
            UserPool.releaseUser(user, bluetoothDevice.getAddress());
            bluetoothDevice = null;
        }
        /**
         * DiscoveredPeer methods
         * ....Ends
         */
    }
}
