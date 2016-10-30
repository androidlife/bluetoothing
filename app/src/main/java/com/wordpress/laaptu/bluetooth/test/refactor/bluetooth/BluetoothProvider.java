package com.wordpress.laaptu.bluetooth.test.refactor.bluetooth;

import android.app.Activity;

import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;
import com.wordpress.laaptu.bluetooth.test.refactor.base.ConnectionMonitor;
import com.wordpress.laaptu.bluetooth.test.refactor.base.SocketCommunicator;

/**
 */

public class BluetoothProvider implements SocketCommunicator.SocketProvider {

    private SocketCommunicator.View view;
    private SocketCommunicator.ClientServerProvider clientServerProvider;
    private SocketCommunicator.PeerProvider peerProvider;
    private ConnectionMonitor connectionMonitor;

    public BluetoothProvider(Activity activity, SocketCommunicator.View view) {
        connectionMonitor = new BluetoothConnectionMonitor(activity, view,
                BluetoothConnectionMonitor.LISTEN_FOR_BLUETOOTH_DEVICE);
        this.view = view;
        clientServerProvider = new BluetoothClientServerProvider(this);
        peerProvider = new BluetoothPeerProvider(view);
    }

    /**
     * Provider implementation
     * ...starts
     */
    @Override
    public void start() {
        connectionMonitor.start();
        if (clientServerProvider != null)
            clientServerProvider.start();
        if (peerProvider != null)
            peerProvider.start();
    }

    @Override
    public void stop() {
        view = null;
        if (connectionMonitor != null) {
            connectionMonitor.stop();
            connectionMonitor = null;
        }

        if (clientServerProvider != null) {
            clientServerProvider.stop();
            clientServerProvider = null;
        }

        if (peerProvider != null) {
            peerProvider.stop();
            peerProvider = null;
        }
    }

    /**
     * ClientServer implementation
     * ...starts
     */
    @Override
    public void connectTo(DiscoveredPeer peer) {
        pauseDiscovery();
        clientServerProvider.connectTo(peer);

    }

    @Override
    public void yesNoMsg(boolean yes) {
        clientServerProvider.yesNoMsg(yes);
    }

    /**
     * PeerProvider implementation
     * ...starts
     */

    @Override
    public void reloadDiscovery() {
        peerProvider.reloadDiscovery();
    }

    @Override
    public void pauseDiscovery() {
        peerProvider.pauseDiscovery();
    }

    /**
     * ViewProvider implementation
     * ...starts
     */
    @Override
    public void connectFrom(DiscoveredPeer peer) {
        pauseDiscovery();
        view.connectFrom(peer);
    }

    @Override
    public void acceptReject(boolean accept) {
        view.acceptReject(accept);
    }
}
