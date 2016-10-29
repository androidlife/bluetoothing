package com.wordpress.laaptu.bluetooth.test.refactor.communication;

import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;
import com.wordpress.laaptu.bluetooth.test.refactor.base.PeerDiscoveryProvider;

import java.util.Collection;

/**
 */

public class CSMain {
    static class CS implements ClientServerProvider.IncomingRequests {

        @Override
        public void connectTo(DiscoveredPeer peer) {

        }

        @Override
        public void yesNoMsg(boolean yes) {

        }

        private ClientServerProvider.OutgoingRequest outgoingRequest;

        public CS(ClientServerProvider.OutgoingRequest outgoingRequest) {
            this.outgoingRequest = outgoingRequest;
        }
    }

    static class UI {
        void someMethod() {
            PeerDiscoveryProvider.OnPeerDiscoveredListener peerDiscoveredListener = new PeerDiscoveryProvider.OnPeerDiscoveredListener() {
                @Override
                public void onPeersDiscovered(Collection<DiscoveredPeer> discoveredPeers) {

                }

                @Override
                public void onSinglePeerDiscovered(DiscoveredPeer discoveredPeer) {

                }

                @Override
                public void onPeersLost(Collection<DiscoveredPeer> lostPeers) {

                }
            };
            //PR.setOnPeerDiscoveryListener();

            ClientServerProvider.OutgoingRequest outgoingRequest = new ClientServerProvider.OutgoingRequest() {
                @Override
                public void connectFrom(DiscoveredPeer peer) {

                }

                @Override
                public void acceptReject(boolean accept) {

                }
            };
            //PR.setOutgoingRequest(Outgoing Request)
        }
    }

    static class PR implements ClientServerProvider.IncomingRequests, PeerDiscoveryProvider, ClientServerProvider, ClientServerProvider.OutgoingRequest {

        @Override
        public void connectTo(DiscoveredPeer peer) {
            //CS.connectTo()
        }

        @Override
        public void yesNoMsg(boolean yes) {
            //CS.yesNoMsg
        }

        private OutgoingRequest outgoingRequest;

        @Override
        public void setOutgoingRequest(OutgoingRequest outgoingRequest) {
            this.outgoingRequest = outgoingRequest;
        }

        @Override
        public void connectFrom(DiscoveredPeer peer) {
            //this will be called from CS
            outgoingRequest.connectFrom(peer);
        }

        @Override
        public void acceptReject(boolean accept) {
            outgoingRequest.acceptReject(accept);
        }


        //List Provider
        @Override
        public void setOnPeerDiscoveredListener(OnPeerDiscoveredListener listener) {
            //ListPR.setOnPeerDiscoveredListener

        }

        @Override
        public void reload() {

        }

        @Override
        public void pause() {

        }


    }

    static class ListPR implements PeerDiscoveryProvider {



        @Override
        public void setOnPeerDiscoveredListener(OnPeerDiscoveredListener listener) {

        }

        @Override
        public void reload() {

        }

        @Override
        public void pause() {
            // pause or stop discovery
        }
    }
}
