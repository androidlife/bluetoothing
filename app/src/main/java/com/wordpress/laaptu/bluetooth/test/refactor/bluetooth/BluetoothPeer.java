package com.wordpress.laaptu.bluetooth.test.refactor.bluetooth;

import android.bluetooth.BluetoothDevice;

import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;
import com.wordpress.laaptu.bluetooth.test.bluetooth.UserPool;

/**
 */

public class BluetoothPeer implements DiscoveredPeer {

    private UserPool.User user;
    private BluetoothDevice bluetoothDevice;

    public BluetoothPeer(BluetoothDevice bluetoothDevice) {
        user = UserPool.getUserByName(bluetoothDevice.getAddress());
        this.bluetoothDevice = bluetoothDevice;
    }

    /**
     * DiscoveredPeer methods
     * ....Starts
     */

    @Override
    public String getName() {
        String name = bluetoothDevice.getName() == null ? "Null" : bluetoothDevice.getName();
        return name;
        //return user.name;
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
    public void connectTo(DiscoveredPeer.ConnectionListener connectionListener) {
    }

    @Override
    public String getUniqueIdentifier() {
        return bluetoothDevice.getAddress();
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

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BluetoothPeer
                && ((BluetoothPeer) obj).getUniqueIdentifier().equals(this.getUniqueIdentifier()));

    }

    @Override
    public int hashCode() {
        return Integer.valueOf(getUniqueIdentifier());
    }

    /**
     * DiscoveredPeer methods
     * ....Ends
     */

}
