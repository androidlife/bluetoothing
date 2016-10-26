package com.wordpress.laaptu.bluetooth.test.bitmaps;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.wordpress.laaptu.bluetooth.test.connect.PeerListAdapter;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Created by laaptu on 10/25/16.
 */

public class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
    public int drawableId = 0;
    private final WeakReference<ImageView> imageViewWeakReference;
    private Resources resources;

    public BitmapWorkerTask(ImageView imageView) {
        this.imageViewWeakReference = new WeakReference<ImageView>(imageView);
        resources = imageView.getResources();
    }


    @Override
    protected Bitmap doInBackground(Integer... params) {
        drawableId = params[0];
        Bitmap bitmap = getSampledBitmap(resources, drawableId,
                params[1], params[2]);
        //PeerListAdapter.addBitmapToMemoryCache(drawableId,bitmap);
        return  bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }
        if (imageViewWeakReference != null && bitmap != null) {
            final ImageView imageView = imageViewWeakReference.get();
            //final BitmapWorkerTask bitmapWorkerTask = PeerListAdapter.getBitmapWorkedTask(imageView);
            BitmapWorkerTask bitmapWorkerTask =null;
            if (bitmapWorkerTask == this && imageView != null){
                imageView.setImageBitmap(bitmap);


            }
        }
    }

    //For ImageLoad Testing
    private int calculateSampleSize(BitmapFactory.Options options, int desiredWidth, int desiredHeight) {
        //sample size 1 means, the same dimension as of original image
        int sampleSize = 1;
        if (options.outHeight > desiredHeight || options.outWidth > desiredWidth) {
            final int halfHeight = options.outHeight;
            final int halfWidth = options.outHeight;
            while ((halfHeight / sampleSize) >= desiredHeight &&
                    (halfWidth / sampleSize) >= desiredWidth)
                sampleSize *= 2;

        }
        return sampleSize;
    }

    private Bitmap getSampledBitmap(Resources res, int drawableId, int desiredWidth, int desiredHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, drawableId, options);
        Timber.d("Original image width =%d , height =%d", options.outWidth, options.outHeight);
        Timber.d("Desired image width =%d , height =%d", desiredWidth, desiredHeight);
        int sampleSize = calculateSampleSize(options, desiredWidth, desiredHeight);
        Timber.d("Sample size =%d", sampleSize);
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeResource(res, drawableId, options);
    }
}
