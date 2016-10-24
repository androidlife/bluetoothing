package com.wordpress.laaptu.bluetooth.test.base;

import android.content.Context;
import android.content.Intent;

/**
 */

public interface NetworkDeviceActivator {
    interface NetworkDeviceActivationListener{
        void onDeviceDeactivated();
        void onDeviceActivated();
    }
    void onActivityResult(int requestCode, int resultCode, Intent data);
    void checkDeviceActivation(Context context, NetworkDeviceActivationListener listener);
    void release();
}
