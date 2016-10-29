package com.wordpress.laaptu.bluetooth.test.refactor.bluetooth;

import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;
import com.wordpress.laaptu.bluetooth.test.refactor.base.SocketCommunicator;

/**
 */

public class BluetoothClientServerProvider implements SocketCommunicator.ClientServerProvider {

    private SocketCommunicator.ViewProvider viewProvider;

    public BluetoothClientServerProvider(SocketCommunicator.ViewProvider viewProvider) {
        this.viewProvider = viewProvider;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        viewProvider = null;

    }

    @Override
    public void connectTo(DiscoveredPeer peer) {

    }

    @Override
    public void yesNoMsg(boolean yes) {

    }
}
