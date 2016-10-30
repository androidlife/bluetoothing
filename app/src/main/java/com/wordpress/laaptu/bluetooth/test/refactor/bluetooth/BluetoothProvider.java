package com.wordpress.laaptu.bluetooth.test.refactor.bluetooth;

import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;
import com.wordpress.laaptu.bluetooth.test.refactor.base.SocketCommunicator;

/**
 */

public class BluetoothProvider implements SocketCommunicator.SocketProvider {

    private SocketCommunicator.View view;
    private SocketCommunicator.ClientServerProvider clientServerProvider;
    private SocketCommunicator.PeerProvider peerProvider;

    public BluetoothProvider(SocketCommunicator.View view) {
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
        clientServerProvider.start();
        peerProvider.start();
    }

    @Override
    public void stop() {
        view = null;
        clientServerProvider.stop();
        peerProvider.stop();
        clientServerProvider = null;
        peerProvider = null;
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
