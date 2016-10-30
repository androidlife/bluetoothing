package com.wordpress.laaptu.bluetooth.test.refactor.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;
import com.wordpress.laaptu.bluetooth.test.bluetooth.StoredBT;
import com.wordpress.laaptu.bluetooth.test.log.Logger;
import com.wordpress.laaptu.bluetooth.test.refactor.base.SocketCommunicator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import timber.log.Timber;

/**
 */

public class BluetoothPeerProvider implements SocketCommunicator.PeerProvider {


    private OnPeerDiscoveredListener listener;
    private final String[] bluetoothDeviceActions = {
            BluetoothDevice.ACTION_FOUND,
            BluetoothAdapter.ACTION_DISCOVERY_STARTED,
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED
    };
    private HashSet<BluetoothDevice> currentDevices, prevDevices;
    private static final int TOTAL_RETRY = 4;
    private int totalRetry;
    private BluetoothAdapter bluetoothAdapter;
    private Activity activity;
    private boolean scanFinished = false;

    private final BroadcastReceiver bluetoothStateBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                onBluetoothDeviceFound(intent);
            } else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                Timber.d("Bluetooth discovery started with retry count = %d", totalRetry);
            } else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                onScanComplete();
            }

        }
    };

    private void onBluetoothDeviceFound(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        //this indicates the first time of scan
        if (prevDevices.size() == 0) {
            //pass the single device to the listener
            // but need to check whether that device is unique or not
            // if unique then only add
            Timber.d("First scan and device found =%s", device.getAddress());
            if (listener != null && !currentDevices.contains(device))
                listener.onSinglePeerDiscovered(new BluetoothPeer(device));
        }
        currentDevices.add(device);
    }

    private void onScanComplete() {
        Timber.d("Bluetooth discovery scan complete with new found devices size = %d", currentDevices.size());
        if (!currentDevices.equals(prevDevices) && prevDevices.size() > 0 && listener != null) {
            //this is for referesh of list
            Timber.d("Old HashSet");
            listener.onPeersLost(convertHashSetToCollection(prevDevices));
            Timber.d("New HashSet");
            listener.onPeersDiscovered(convertHashSetToCollection(currentDevices));


        }
        Timber.d("No new device discovery");
        for (BluetoothDevice device : currentDevices)
            Timber.d("Device id = %s", device.getAddress());
        Timber.d("------------------------");
        prevDevices = new HashSet<>(currentDevices);
        currentDevices.clear();
        if (prevDevices.size() == 0)
            retryDiscovery();
        else {
            setScanFinished(true);
            cancelBluetoothDiscovery();
        }
    }

    private void retryDiscovery() {
        cancelBluetoothDiscovery();
        if (totalRetry > 0) {
            --totalRetry;
            bluetoothAdapter.startDiscovery();
        }
    }

    @Override
    public void reloadDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            Logger.d("Bluetooth discovery is in progress, can't refresh");
            return;
        }
        totalRetry = TOTAL_RETRY;
        setScanFinished(false);
        cancelBluetoothDiscovery();
        bluetoothAdapter.startDiscovery();
    }

    private void setScanFinished(boolean scanFinished) {
        this.scanFinished = scanFinished;
    }

    private void cancelBluetoothDiscovery() {
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
    }

    public BluetoothPeerProvider(Activity activity, OnPeerDiscoveredListener listener) {
        this.listener = listener;
        this.activity = activity;
    }

    @Override
    public void start() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IntentFilter bluetoothStateIntentFilter = new IntentFilter();
        for (String action : bluetoothDeviceActions)
            bluetoothStateIntentFilter.addAction(action);
        activity.registerReceiver(bluetoothStateBR, bluetoothStateIntentFilter);
        currentDevices = new HashSet<>();
        prevDevices = new HashSet<>();
        reloadDiscovery();
        //workWithSavedItems();
    }

    /**
     * For testing purpose only
     */
    private void workWithSavedItems() {
        BluetoothDevice bluetoothDevice = StoredBT.getInstance().getBluetoothDevice();
        if (listener != null) {
            Collection<DiscoveredPeer> devices = new ArrayList<>();
            DiscoveredPeer peer = new BluetoothPeer(bluetoothDevice);
            devices.add(peer);
            listener.onPeersDiscovered(devices);
        }
    }

    @Override
    public void stop() {
        try {
            activity.unregisterReceiver(bluetoothStateBR);
        } catch (Exception e) {

        } finally {
            activity = null;
        }

        cancelBluetoothDiscovery();
        listener = null;
        if (currentDevices != null) {
            currentDevices.clear();
            prevDevices.clear();
        }
        currentDevices = null;
        prevDevices = null;
        totalRetry = 0;
    }

    private Collection<DiscoveredPeer> convertHashSetToCollection(HashSet<BluetoothDevice> hashSet) {
        Collection<DiscoveredPeer> peerList = new ArrayList<>(hashSet.size());
        Iterator<BluetoothDevice> iterator = hashSet.iterator();
        while (iterator.hasNext()) {
            BluetoothDevice device = iterator.next();
            Timber.d("Bluetooth device = %s", device.getAddress());
            peerList.add(new BluetoothPeer(device));
        }
        return peerList;
    }


    @Override
    public void pauseDiscovery() {
        cancelBluetoothDiscovery();
    }
}
