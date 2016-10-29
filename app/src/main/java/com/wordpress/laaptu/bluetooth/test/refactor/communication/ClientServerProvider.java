package com.wordpress.laaptu.bluetooth.test.refactor.communication;

import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;

/**
 */

public interface ClientServerProvider {
    interface OutgoingRequest {
        void connectFrom(DiscoveredPeer peer);

        void acceptReject(boolean accept);
    }

    interface IncomingRequests {
        void connectTo(DiscoveredPeer peer);

        void yesNoMsg(boolean yes);
    }

    void setOutgoingRequest(OutgoingRequest request);
}
