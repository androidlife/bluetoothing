package com.wordpress.laaptu.bluetooth.test.refactor.bluetooth;

import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;
import com.wordpress.laaptu.bluetooth.test.refactor.base.ClientServerProvider;
import com.wordpress.laaptu.bluetooth.test.refactor.base.PeerDiscoveryProvider;
import com.wordpress.laaptu.bluetooth.test.refactor.base.Provider;

import java.util.Collection;

/**
 */

public class MainUI implements UIProvider {
    @Override
    public void connectFrom(DiscoveredPeer peer) {

    }

    @Override
    public void acceptReject(boolean accept) {

    }


    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void setIncomingActions(ClientServerProvider.IncomingActions incomingActions) {

    }

    @Override
    public void onPeersDiscovered(Collection<DiscoveredPeer> discoveredPeers) {

    }

    @Override
    public void onSinglePeerDiscovered(DiscoveredPeer discoveredPeer) {

    }

    @Override
    public void onPeersLost(Collection<DiscoveredPeer> lostPeers) {

    }
}
