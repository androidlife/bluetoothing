package com.wordpress.laaptu.bluetooth.test.refactor.base;

/**
 * An interface whose implementation must find out the
 * ways in which connection has been lost and notify
 *  the concerned party about the lost connection
 *
 */

public interface ConnectionMonitor extends Provider {
    interface OnConnectionListener {
        void connectionLost();
    }

    void setOnConnectionListener(OnConnectionListener onConnectionListener);
}
