package com.wordpress.laaptu.bluetooth.test.refactor.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wordpress.laaptu.bluetooth.R;
import com.wordpress.laaptu.bluetooth.test.bitmaps.loaders.ImageFetcher;
import com.wordpress.laaptu.bluetooth.test.refactor.Extras;
import com.wordpress.laaptu.bluetooth.test.refactor.UserPool;
import com.wordpress.laaptu.bluetooth.test.refactor.base.DiscoveredPeer;
import com.wordpress.laaptu.bluetooth.test.refactor.base.SocketCommunicator;
import com.wordpress.laaptu.bluetooth.test.refactor.bluetooth.BluetoothProvider;

import java.util.ArrayList;
import java.util.Collection;

import timber.log.Timber;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 */

public class OnlineFragment extends Fragment implements PeerListAdapter.OnItemClickListener, SocketCommunicator.View {

    String action = Extras.ACTION_TOUCHCHAT, username = UserPool.getDefaultUserName();
    private int dialogStyle;
    private int connectingBackgroundId;
    private int backgroundId = -1;
    private RecyclerView contactList;
    private ImageFetcher imageFetcher;
    private PeerListAdapter peerAdapter;
    private ArrayList<DiscoveredPeer> staticPeers;
    private SocketCommunicator.SocketProvider socketProvider;


    //All Implemented interface starts here


    /**
     * Provider interface
     * ...starts
     */
    @Override
    public void start() {
        startUI();
        socketProvider = new BluetoothProvider(getActivity(), this, action, username);
        socketProvider.start();
    }

    @Override
    public void stop() {
        stopUI();
        if (socketProvider != null) {
            socketProvider.stop();
            socketProvider = null;
        }

    }

    /**
     * PeerDiscoveryProvider.OnPeerDiscoveredListener interface
     * ...starts
     * will be called from PeerProvider implemented class
     */
    @Override
    public void onPeersDiscovered(Collection<DiscoveredPeer> discoveredPeers) {
        if (peerAdapter != null) {
            for (DiscoveredPeer peer : discoveredPeers)
                peerAdapter.addAt(0, peer);
            peerAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onSinglePeerDiscovered(DiscoveredPeer discoveredPeer) {
        if (peerAdapter != null)
            peerAdapter.addNewPeer(discoveredPeer);

    }

    @Override
    public void onPeersLost(Collection<DiscoveredPeer> lostPeers) {
        if (peerAdapter != null) {
            for (DiscoveredPeer peer : lostPeers) {
                peerAdapter.remove(peer);
            }
        }

    }

    /**
     * ViewProvider interface
     * ...starts
     * will be called from ClientServer
     */
    @Override
    public void connectFrom(DiscoveredPeer peer) {

    }

    @Override
    public void acceptReject(boolean accept) {

    }


    /**
     * Connection Lost
     */
    @Override
    public void connectionLost() {
        stop();
        getActivity().finish();
    }

    //-------------------------------------------------------


    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume()");
        start();
    }

    private void startUI() {
        imageFetcher = new ImageFetcher(getActivity(), getResources().getDimensionPixelSize(R.dimen.contact_pic_size));
        contactList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (imageFetcher == null)
                    return;
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    imageFetcher.setPauseWork(false);
                else
                    imageFetcher.setPauseWork(true);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        peerAdapter = new PeerListAdapter(getActivity(), contactList, imageFetcher);
        UserPool.setContext(getActivity());
        staticPeers = new ArrayList<>();
        staticPeers.add(new UserPool.StaticPeer(UserPool.getBusyUser(), "Busy", 3));
        UserPool.User[] offlineUsers = UserPool.getOfflineUsers();
        for (UserPool.User offlineUser : offlineUsers) {
            staticPeers.add(new UserPool.StaticPeer(offlineUser, "Offline", 4));
        }

        peerAdapter.addAll(staticPeers);
        peerAdapter.setOnItemClickListener(this);
        contactList.setAdapter(peerAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause()");
        stop();
    }

    private void stopUI() {
        if (peerAdapter != null) {
            peerAdapter.clearAll();
            peerAdapter = null;
        }
        if (contactList != null) {
            contactList.setAdapter(null);
        }

        if (staticPeers != null) {
            staticPeers.clear();
            staticPeers = null;
        }

        if (imageFetcher != null) {
            imageFetcher.setExitTasksEarly(true);
            imageFetcher.clearCache();
            imageFetcher = null;
        }
    }

    @Override
    public void onItemClicked(DiscoveredPeer peer) {

    }

    /**
     * All view related stuff
     * background,layouts
     */
    public OnlineFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_online, container, false);
        contactList = (RecyclerView) view.findViewById(R.id.contacts_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        contactList.setLayoutManager(linearLayoutManager);
        return view;
    }

    public void passIntent(Intent intent) {
        if (intent == null)
            return;
        String action = intent.getStringExtra(Extras.EXTRA_ACTION);
        if (TextUtils.isEmpty(action))
            action = Extras.ACTION_TOUCHCHAT;
        setThemeAsPerAction(action);
        username = intent.getStringExtra(Extras.EXTRA_USERNAME);
    }


    private void setThemeAsPerAction(String action) {
        this.action = action;
        boolean isDark = false;
        if (Extras.ACTION_TOUCHTRAILS.equals(action)) {
            backgroundId = R.color.black;
            connectingBackgroundId = R.color.black;
            isDark = true;
        } else if (Extras.ACTION_TOUCHVIDEO.equals(action)) {
            backgroundId = R.drawable.bg_video_connecting;
            connectingBackgroundId = R.drawable.bg_video_connecting_progress;
        } else if (Extras.ACTION_TOUCHDICE.equals(action)) {
            backgroundId = R.drawable.bg_dice_connecting;
            connectingBackgroundId = R.drawable.bg_dice_connecting;
            isDark = true;
        } else if (Extras.ACTION_TOUCHCHAT.equals(action)) {
            backgroundId = R.drawable.bg_chat_connecting;
            connectingBackgroundId = R.drawable.bg_chat_connecting_progress;
        }
        if (isDark) {
            dialogStyle = android.R.style.Theme_Holo_Panel;
        } else {
            dialogStyle = android.R.style.Theme_Holo_Light_Panel;
        }
    }


}
