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
import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;
import com.wordpress.laaptu.bluetooth.test.bitmaps.loaders.ImageFetcher;
import com.wordpress.laaptu.bluetooth.test.log.Logger;
import com.wordpress.laaptu.bluetooth.test.refactor.Extras;
import com.wordpress.laaptu.bluetooth.test.refactor.UserPool;

import java.util.ArrayList;

import timber.log.Timber;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 */

public class OnlineFragment extends Fragment implements PeerListAdapter.OnItemClickListener {

    String action = Extras.ACTION_TOUCHCHAT, username = UserPool.getDefaultUserName();
    private int dialogStyle;
    private int connectingBackgroundId;
    private int backgroundId = -1;
    private RecyclerView contactList;
    private ImageFetcher imageFetcher;
    private PeerListAdapter peerAdapter;
    private ArrayList<DiscoveredPeer> staticPeers;


    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume()");
        startUI();
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
        stopUI();
    }

    private void stopUI() {
        if (contactList != null) {
            contactList.addOnScrollListener(null);
            contactList.setAdapter(null);
        }
        if (peerAdapter != null) {
            peerAdapter.clearAll();
            peerAdapter = null;
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
