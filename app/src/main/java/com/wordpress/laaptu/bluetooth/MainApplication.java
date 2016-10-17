package com.wordpress.laaptu.bluetooth;

import android.app.Application;
import timber.log.Timber;

/**
 * Created by laaptu on 10/7/16.
 */

public class MainApplication extends Application {
  @Override public void onCreate() {
    super.onCreate();
    Timber.plant(new Timber.DebugTree());
  }
}
