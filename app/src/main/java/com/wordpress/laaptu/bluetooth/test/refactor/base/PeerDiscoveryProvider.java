package com.wordpress.laaptu.bluetooth.test.refactor.base;

import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;

import java.util.Collection;

/**
 */

public interface PeerDiscoveryProvider extends Provider{
    interface OnPeerDiscoveredListener {
        void onPeersDiscovered(Collection<DiscoveredPeer> discoveredPeers);
        void onSinglePeerDiscovered(DiscoveredPeer discoveredPeer);
        void onPeersLost(Collection<DiscoveredPeer> lostPeers);
    }
    void setOnPeerDiscoveredListener(OnPeerDiscoveredListener listener);
}
