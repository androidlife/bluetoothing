package com.wordpress.laaptu.bluetooth.test.refactor;

import android.app.Activity;
import android.content.Intent;

import com.wordpress.laaptu.bluetooth.test.refactor.ui.ChatActivity;

import java.util.UUID;

/**
 */

public class IntentUtils {
    private IntentUtils() {

    }

    public static class ServerInfo {
        public static final String SERVER_NAME = "LiveTouchChatServer";
        public static final UUID SERVER_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a67");
        public static final String SERVER_NAME1 = "LiveTouchChatServer1";
        public static final UUID SERVER_UUID1 = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a69");
    }

    public static class Extras {
        public static final String ACTION = "action";
        public static final String USERNAME = "username";
        public static final String MEDIUM = "medium";
        public static final String ADDRESS = "ipAddress";
        public static final String ISHOST = "isHost";
        public static final String PEERNAME = "peername";

        public static final String ACTION_TOUCHTRAILS = "TouchTrails";
        public static final String ACTION_TOUCHVIDEO = "TouchVideo";
        public static final String ACTION_TOUCHDICE = "TouchDice";
        public static final String ACTION_TOUCHCHAT = "TouchChat";
        //TODO make this build variables,so that it reflects package name change
        public static final String ACTION_ = "com.immersion.livetouch.action.";

        public static final String MEDIUM_WIFI = "direct";
        public static final String MEDIUM_BLUETOOTH = "bluetooth";
    }

    /**
     * This must in following format
     * params[]
     * action
     * medium
     * ipaddress
     * isHost
     * peername
     */
    public static void navigateToChat(Activity activity, boolean isHost, String... params) {
        if (params == null || params.length != 4)
            throw new RuntimeException("Illegal parameter passed for activity navigation");

        Intent intent = new Intent(Extras.ACTION_ + params[0]);
        intent = new Intent(activity, ChatActivity.class);
        intent.putExtra(Extras.MEDIUM, params[1]);
        intent.putExtra(Extras.ADDRESS, params[2]);
        intent.putExtra(Extras.PEERNAME, params[3]);
        intent.putExtra(Extras.ISHOST, isHost);
        activity.startActivityForResult(intent, 0);

    }

}
