package com.wordpress.laaptu.bluetooth.test.refactor.bluetooth;

import com.wordpress.laaptu.bluetooth.test.refactor.base.ClientServerProvider;
import com.wordpress.laaptu.bluetooth.test.refactor.base.PeerDiscoveryProvider;
import com.wordpress.laaptu.bluetooth.test.refactor.base.Provider;

/**
 */

public interface UIProvider extends ClientServerProvider.OutgoingActions,Provider,ClientServerProvider.OnIncomingActions,PeerDiscoveryProvider.OnPeerDiscoveredListener {
}
