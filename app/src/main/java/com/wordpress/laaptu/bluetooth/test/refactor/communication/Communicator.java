package com.wordpress.laaptu.bluetooth.test.refactor.communication;

import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;

/**
 */

public interface Communicator {
    interface ConnectToYesNo {
        void connectTo(DiscoveredPeer peer);

        void yesNoMsg(boolean yes);
    }

    interface AcceptRejectConnectFrom {
        void connectFrom(DiscoveredPeer peer);

        void acceptReject(boolean accept);
    }

    interface OnAcceptRejectConnectFrom {
        void setAcceptRejectConnectFrom(AcceptRejectConnectFrom acceptRejectConnectFrom);
    }
}
