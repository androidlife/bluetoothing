package com.wordpress.laaptu.bluetooth.test.refactor.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.wordpress.laaptu.bluetooth.test.bluetooth.UserPool;
import com.wordpress.laaptu.bluetooth.test.refactor.Extras;

/**
 */

public class OnlineFragment extends Fragment {

    String action = Extras.ACTION_TOUCHCHAT, username = UserPool.getDefaultUserName();

    public OnlineFragment() {

    }

    public void passIntent(Intent intent) {
        if (intent == null)
            return;
        String action = intent.getStringExtra(Extras.EXTRA_ACTION);
        if (TextUtils.isEmpty(action))
            return;
        this.action = action;
        username = intent.getStringExtra(Extras.EXTRA_USERNAME);
    }
}
