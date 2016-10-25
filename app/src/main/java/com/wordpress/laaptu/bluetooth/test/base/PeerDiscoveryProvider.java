package com.wordpress.laaptu.bluetooth.test.base;

import java.util.Collection;

public interface PeerDiscoveryProvider {
    public interface OnPeerDiscoveredListener {
        public void onPeersDiscovered(Collection<DiscoveredPeer> discoveredPeers);
        void onSinglePeerDiscovered(DiscoveredPeer discoveredPeer);
        public void onPeersLost(Collection<DiscoveredPeer> lostPeers);
    }

    interface NetworkDeviceListener {
        void onNetworkDeviceLost();
    }

    public void start();

    void start(NetworkDeviceListener networkDeviceListener);

    public void stop();

    public void reload();

    public void setIdentifier(String newIdentifier);

    public void setOnPeerDiscoveredListener(OnPeerDiscoveredListener listener);

    public void setAction(String action);


}
