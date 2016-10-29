package com.wordpress.laaptu.bluetooth.test.refactor.communication;

import com.wordpress.laaptu.bluetooth.test.refactor.base.PeerDiscoveryProvider;

/**
 */

public interface SocketCommunication {
    interface View extends PeerDiscoveryProvider.OnPeerDiscoveredListener,ClientServerProvider.OutgoingRequest{

    }
    interface ClientServer extends ClientServerProvider.IncomingRequests{

    }
    interface ListProvider extends PeerDiscoveryProvider{

    }
    interface Provider extends ClientServerProvider.IncomingRequests, PeerDiscoveryProvider, ClientServerProvider, ClientServerProvider.OutgoingRequest{

    }
}
