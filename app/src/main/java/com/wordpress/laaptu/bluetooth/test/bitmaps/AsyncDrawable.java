package com.wordpress.laaptu.bluetooth.test.bitmaps;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

/**
 * Created by laaptu on 10/25/16.
 */

public class AsyncDrawable extends BitmapDrawable {
    private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskWeakReference;

    public AsyncDrawable(Resources res, Bitmap bitmap,BitmapWorkerTask bitmapWorkerTask){
        super(res,bitmap);
        bitmapWorkerTaskWeakReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
    }
    public BitmapWorkerTask getBitmapWorkerTask(){
        return  bitmapWorkerTaskWeakReference.get();
    }
}
