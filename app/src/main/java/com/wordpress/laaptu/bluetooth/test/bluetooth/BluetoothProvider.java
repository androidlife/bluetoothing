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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import timber.log.Timber;

/**
 */

public class BluetoothProvider implements PeerDiscoveryProvider {

    private final Activity activity;
    private IntentFilter bluetoothStateIntentFilter;
    private NetworkDeviceListener networkDeviceListener;
    private BluetoothAdapter bluetoothAdapter;
    private final String[] bluetoothDeviceActions = {
            BluetoothAdapter.ACTION_STATE_CHANGED,
            BluetoothDevice.ACTION_FOUND,
            BluetoothAdapter.ACTION_DISCOVERY_STARTED,
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED
    };
    //private boolean firstScan = true;
    private HashSet<BluetoothDevice> currentDevices, prevDevices;
    private static final int TOTAL_RETRY = 4;
    private int totalRetry;
    private OnPeerDiscoveredListener listener;

    public BluetoothProvider(Activity activity) {
        this.activity = activity;
    }


    private final BroadcastReceiver bluetoothStateBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                onBluetoothStateChanged(intent);
            } else if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                onBluetoothDeviceFound(intent);
            } else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                Timber.d("Bluetooth discovery started with retry count = %d", totalRetry);
            } else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                onScanComplete();
            }

        }
    };


    private void onScanComplete() {
        Timber.d("Bluetooth discovery scan complete with new found devices size = %d", currentDevices.size());
        if (!currentDevices.equals(prevDevices) && prevDevices.size() > 0) {
            //then only pass to the interface
            //this is for first time
            //peer discovery lost ( prevDevices)
            // peer discovery found( currentDevices)
            if(listener !=null){
                listener.onPeersLost(convertHashSetToCollection(prevDevices));
                listener.onPeersDiscovered(convertHashSetToCollection(currentDevices));
            }

        }
        Timber.d("No new device discovery");
        for (BluetoothDevice device : currentDevices)
            Timber.d("Device id = %s", device.getAddress());
        Timber.d("------------------------");
        prevDevices = new HashSet<>(currentDevices);
        currentDevices.clear();
        startDiscovery();
    }

    private void onBluetoothDeviceFound(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        //this indicates the first time of scan
        if (prevDevices.size() == 0) {
            //pass the single device to the listener
            // but need to check whether that device is unique or not
            // if unique then only add
            Timber.d("First scan and device found =%s", device.getAddress());
            if (listener != null)
                listener.onSinglePeerDiscovered(new Peer(device));
        }
        currentDevices.add(device);
    }

    private void startDiscovery() {
        cancelBluetoothDiscovery();
        if (totalRetry > 0) {
            --totalRetry;
            bluetoothAdapter.startDiscovery();
        }
    }

    private void cancelBluetoothDiscovery() {
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
    }


    /**
     * Method to check if bluetooth device is powered off or not
     */
    private void onBluetoothStateChanged(Intent intent) {
        int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
        switch (bluetoothState) {
            case BluetoothAdapter.STATE_ON:
                break;
            case BluetoothAdapter.ERROR:
            case BluetoothAdapter.STATE_OFF:
                Timber.d("Bluetooth is powered off");
                notifyAndStopAll();
                break;
            default:
                break;
        }
    }

    private void notifyAndStopAll() {
        Timber.d("Stopping bluetoothprovider from notifyAndStopAll");
        if (networkDeviceListener != null) {
            networkDeviceListener.onNetworkDeviceLost();
        }
        stop();

    }

    /**
     * PeerDiscoveryProvider methods
     * ....Starts
     */
    @Override
    public void start() {
        if (bluetoothAdapter == null)
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            notifyAndStopAll();
            return;
        }
        bluetoothStateIntentFilter = new IntentFilter();
        for (String action : bluetoothDeviceActions)
            bluetoothStateIntentFilter.addAction(action);
        activity.registerReceiver(bluetoothStateBR, bluetoothStateIntentFilter);

        //starting server should be done here
        totalRetry = TOTAL_RETRY;
        startDiscovery();

        //firstScan = true;
        currentDevices = new HashSet<>();
        prevDevices = new HashSet<>();
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


        cancelBluetoothDiscovery();
        if (listener != null) {
            listener.onPeersLost(convertHashSetToCollection(prevDevices));
            listener = null;
        }
        currentDevices.clear();
        prevDevices.clear();
        currentDevices = null;
        prevDevices = null;
        totalRetry = 0;
    }

    private Collection<DiscoveredPeer> convertHashSetToCollection(HashSet<BluetoothDevice> hashSet) {
        Collection<DiscoveredPeer> peerList = new ArrayList<>(hashSet.size());
        Iterator<BluetoothDevice> iterator = hashSet.iterator();
        while (iterator.hasNext()) {
            peerList.add(new Peer(iterator.next()));
        }
        return peerList;
    }

    @Override
    public void reload() {

    }

    @Override
    public void setIdentifier(String newIdentifier) {

    }

    @Override
    public void setOnPeerDiscoveredListener(OnPeerDiscoveredListener listener) {
        this.listener = listener;
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
