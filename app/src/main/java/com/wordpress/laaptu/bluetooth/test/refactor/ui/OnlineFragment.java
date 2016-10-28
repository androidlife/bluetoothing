package com.wordpress.laaptu.bluetooth.test.refactor.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.wordpress.laaptu.bluetooth.R;
import com.wordpress.laaptu.bluetooth.test.bluetooth.UserPool;
import com.wordpress.laaptu.bluetooth.test.refactor.Extras;

/**
 */

public class OnlineFragment extends Fragment {

    String action = Extras.ACTION_TOUCHCHAT, username = UserPool.getDefaultUserName();
    private int dialogStyle;
    private int connectingBackgroundId;
    private int backgroundid = -1;

    public OnlineFragment() {

    }

    public void passIntent(Intent intent) {
        if (intent == null)
            return;
        String action = intent.getStringExtra(Extras.EXTRA_ACTION);
        if (TextUtils.isEmpty(action))
            return;
        setThemeAsPerAction(action);
        username = intent.getStringExtra(Extras.EXTRA_USERNAME);
    }


    private void setThemeAsPerAction(String action) {
        this.action = action;
        boolean isDark = false;
        if (Extras.ACTION_TOUCHTRAILS.equals(action)) {
            backgroundid = R.color.black;
            connectingBackgroundId = R.color.black;
            isDark = true;
        } else if (Extras.ACTION_TOUCHVIDEO.equals(action)) {
            backgroundid = R.drawable.bg_video_connecting;
            connectingBackgroundId = R.drawable.bg_video_connecting_progress;
        } else if (Extras.ACTION_TOUCHDICE.equals(action)) {
            backgroundid = R.drawable.bg_dice_connecting;
            connectingBackgroundId = R.drawable.bg_dice_connecting;
            isDark = true;
        } else if (Extras.ACTION_TOUCHCHAT.equals(action)) {
            backgroundid = R.drawable.bg_chat_connecting;
            connectingBackgroundId = R.drawable.bg_chat_connecting_progress;
        }
        if (isDark) {
            dialogStyle = android.R.style.Theme_Holo_Panel;
        } else {
            dialogStyle = android.R.style.Theme_Holo_Light_Panel;
        }
    }
}
