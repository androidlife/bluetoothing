package com.wordpress.laaptu.bluetooth.test.refactor.base;

import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;

/**
 * TODO refactor interface name
 */

public interface ClientServerProvider {

    interface OnOutgoingActions {
        void setOutgoingActions(OutgoingActions outgoingActions);
    }
    interface OnIncomingActions{
        void setIncomingActions(IncomingActions incomingActions);
    }

    interface IncomingActions extends OnOutgoingActions {
        void connectTo(DiscoveredPeer peer);

        void writeYesNoMsg(boolean yes);
    }


    interface OutgoingActions {
        void connectFrom(DiscoveredPeer peer);

        void acceptReject(boolean accept);
    }
}
