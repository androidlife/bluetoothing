package com.wordpress.laaptu.bluetooth.test.refactor.communication;

import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;

/**
 * Created by laaptu on 10/28/16.
 */

public class ProviderMain implements Communicator.ConnectToYesNo, Communicator.OnAcceptRejectConnectFrom,Communicator.AcceptRejectConnectFrom {

    private ClientServerMain clientServerMain;
    private Communicator.AcceptRejectConnectFrom acceptRejectConnectFrom;


    @Override
    public void connectTo(DiscoveredPeer peer) {
        clientServerMain.connectTo(peer);
    }

    @Override
    public void yesNoMsg(boolean yes) {
        clientServerMain.yesNoMsg(yes);

    }

    @Override
    public void setAcceptRejectConnectFrom(Communicator.AcceptRejectConnectFrom acceptRejectConnectFrom) {
        this.acceptRejectConnectFrom = acceptRejectConnectFrom;

    }

    @Override
    public void connectFrom(DiscoveredPeer peer) {

    }

    @Override
    public void acceptReject(boolean accept) {

    }
}
