package com.wordpress.laaptu.bluetooth.test.bluetooth;

import android.app.Activity;

/**
 */

public interface ConnectionMonitor {
    interface OnConnectionLostListener {
        void onConnectionLost();
    }

    void start(Activity activity, OnConnectionLostListener listener);

    void stop();
}
