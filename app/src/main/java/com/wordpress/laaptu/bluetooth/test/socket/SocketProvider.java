package com.wordpress.laaptu.bluetooth.test.socket;

import android.bluetooth.BluetoothSocket;

/**
 * Created by laaptu on 10/26/16.
 */

public class SocketProvider {
    private SocketProvider() {

    }

    private static SocketProvider instance;

    public static SocketProvider getInstance() {
        if (instance == null)
            instance = new SocketProvider();
        return instance;
    }

    public String address;
    public boolean isServer;
}
