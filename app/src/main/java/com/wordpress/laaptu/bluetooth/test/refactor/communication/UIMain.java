package com.wordpress.laaptu.bluetooth.test.refactor.communication;

import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;

/**
 * Created by laaptu on 10/28/16.
 */

public class UIMain  {
    private ProviderMain providerMain;

    public void someFunction(){
        providerMain.setAcceptRejectConnectFrom(new Communicator.AcceptRejectConnectFrom() {
            @Override
            public void connectFrom(DiscoveredPeer peer) {

            }

            @Override
            public void acceptReject(boolean accept) {

            }
        });
    }
}
