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
import com.wordpress.laaptu.bluetooth.test.bluetooth.UserPool;
import com.wordpress.laaptu.bluetooth.test.refactor.Extras;

/**
 */

public class OnlineFragment extends Fragment {

    String action = Extras.ACTION_TOUCHCHAT, username = UserPool.getDefaultUserName();
    private int dialogStyle;
    private int connectingBackgroundId;
    private int backgroundId = -1;
    private RecyclerView contactList;




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
