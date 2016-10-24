package com.wordpress.laaptu.bluetooth.test.base;

import java.util.Collection;

public interface PeerDiscoveryProvider {
    public interface OnPeerDiscoveredListener {
        public void onPeersDiscovered(Collection<DiscoveredPeer> discoveredPeers);

        public void onPeersLost(Collection<DiscoveredPeer> lostPeers);
    }

    interface NetworkDeviceListener {
        void onNetworkDeviceLost();
    }

    public void start();

    public void stop();

    public void reload();

    public void setIdentifier(String newIdentifier);

    public void setOnPeerDiscoveredListener(OnPeerDiscoveredListener listener);

    public void setAction(String action);

    void start(NetworkDeviceListener networkDeviceListener);
}
