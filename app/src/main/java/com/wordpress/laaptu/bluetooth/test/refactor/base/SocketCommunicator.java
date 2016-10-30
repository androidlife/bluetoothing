package com.wordpress.laaptu.bluetooth.test.refactor.base;

import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;
import com.wordpress.laaptu.bluetooth.test.refactor.base.ConnectionMonitor;

import java.util.Collection;

/**
 */

public class SocketCommunicator {
    public interface ViewProvider {
        void connectFrom(DiscoveredPeer peer);

        void acceptReject(boolean accept);
    }

    interface ClientServer {
        void connectTo(DiscoveredPeer peer);

        void yesNoMsg(boolean yes);
    }


    interface PeerDiscoveryProvider {
        interface OnPeerDiscoveredListener {
            void onPeersDiscovered(Collection<DiscoveredPeer> discoveredPeers);

            void onSinglePeerDiscovered(DiscoveredPeer discoveredPeer);

            void onPeersLost(Collection<DiscoveredPeer> lostPeers);
        }

        //void setOnPeerDiscoveredListener(PeerDiscoveryProvider.OnPeerDiscoveredListener listener);

        void reloadDiscovery();

        void pauseDiscovery();
    }

    public interface View extends ViewProvider, ConnectionMonitor.OnConnectionListener,PeerDiscoveryProvider.OnPeerDiscoveredListener, Provider {
    }

    public interface SocketProvider extends ViewProvider, PeerDiscoveryProvider, ClientServer, Provider {
    }

    public interface PeerProvider extends PeerDiscoveryProvider, Provider {

    }

    public interface ClientServerProvider extends ClientServer, Provider {

    }
}
