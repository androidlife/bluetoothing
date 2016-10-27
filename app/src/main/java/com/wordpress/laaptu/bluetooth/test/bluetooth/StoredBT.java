package com.wordpress.laaptu.bluetooth.test.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * Created by laaptu on 10/27/16.
 */
public class StoredBT {

    public static final String BT_NOTE3 = "38:94:96:F2:37:60";
    public static final String BT_NEXUS6 = "F8:CF:C5:D4:7D:32";
    private static StoredBT ourInstance = new StoredBT();

    public static StoredBT getInstance() {
        return ourInstance;
    }

    private StoredBT() {
    }

    public BluetoothDevice getBluetoothDevice() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter.getAddress().equals(BT_NOTE3))
            return adapter.getRemoteDevice(BT_NEXUS6);
        return adapter.getRemoteDevice(BT_NOTE3);
    }

}
