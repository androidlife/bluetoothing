package com.wordpress.laaptu.bluetooth.test.refactor.bluetooth;

import com.wordpress.laaptu.bluetooth.test.refactor.base.SocketCommunicator;

/**
 */

public class BluetoothPeerProvider implements SocketCommunicator.PeerProvider {


    private OnPeerDiscoveredListener onPeerDiscoveredListener;

    public BluetoothPeerProvider(OnPeerDiscoveredListener listener) {
        this.onPeerDiscoveredListener = listener;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        onPeerDiscoveredListener = null;
    }


    @Override
    public void reloadDiscovery() {

    }

    @Override
    public void pauseDiscovery() {

    }
}
