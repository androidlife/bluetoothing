package com.wordpress.laaptu.bluetooth.test.log;

import android.support.annotation.NonNull;


import timber.log.Timber;

import static android.R.id.message;


/**
 * Wrapper class for logging offering static method calls
 */

public final class Logger {

    private static Logger instance;

    public static void initLogger(){
        Timber.plant(new Timber.DebugTree());
    }

    /**
     *
     * @param message
     * @param objects
     */
    public static void v(String message, Object... objects){
        Timber.v(message, objects);

    }

    /**
     *
     * @param t
     * @param message
     * @param objects
     */
    public static void v(Throwable t, String message, Object... objects){
        Timber.v(t, message, objects);

    }

    /**
     *
     * @param message
     * @param objects
     */
    public static void d(@NonNull  String message, Object... objects){
        Timber.d(message, objects);

    }

    /**
     *
     * @param t
     * @param message
     * @param objects
     */
    public static void d(Throwable t, String message, Object... objects){
        Timber.d(t, message, objects);

    }


    /**
     *
     * @param message
     * @param objects
     */
    public static void i(String message, Object... objects){
        Timber.i(message, objects);

    }

    /**
     *
     * @param t
     * @param message
     * @param objects
     */
    public static void i(Throwable t, String message, Object... objects){
        Timber.i(t, message, objects);

    }

    /**
     *
     * @param message
     * @param objects
     */
    public static void w(String message, Object... objects){
        Timber.w(message, objects);

    }

    /**
     *
     * @param t
     * @param message
     * @param objects
     */
    public static void w(Throwable t, String message, Object... objects){
        Timber.w(t, message, objects);

    }

    /**
     *
     * @param message
     * @param objects
     */
    public static void e(String message, Object... objects){
        Timber.e(message, objects);

    }

    /**
     *
     * @param t
     * @param message
     * @param objects
     */
    public static void e(Throwable t, String message, Object... objects){
        Timber.e(t, message, objects);

    }

}
