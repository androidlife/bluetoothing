package com.wordpress.laaptu.bluetooth.test.connect;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wordpress.laaptu.bluetooth.R;
import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;
import com.wordpress.laaptu.bluetooth.test.bitmaps.loaders.ImageFetcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class PeerListAdapter extends RecyclerView.Adapter<PeerListAdapter.ViewHolder> {
    private LayoutInflater layoutInflater;
    private ArrayList<DiscoveredPeer> peerList;
    private ImageFetcher imageFetcher;
    private RecyclerView recyclerView;

    public PeerListAdapter(Activity activity,RecyclerView recyclerView,ImageFetcher imageFetcher) {
        layoutInflater = LayoutInflater.from(activity);
        peerList = new ArrayList<>();

        this.imageFetcher = imageFetcher;
        this.recyclerView = recyclerView;
    }


    public void addAll(Collection<? extends DiscoveredPeer> collection) {
        peerList.addAll(collection);
    }


    public void clear() {
        peerList.clear();
    }


    public void add(DiscoveredPeer object) {
        peerList.add(object);
    }


    public void remove(DiscoveredPeer object) {
        peerList.remove(object);
    }


    public void sort(Comparator<? super DiscoveredPeer> comparator) {
        Collections.sort(peerList, comparator);
    }

    public synchronized void addNewPeer(DiscoveredPeer peer) {
        peerList.add(0, peer);
        notifyItemInserted(0);
        recyclerView.scrollToPosition(0);

    }


    public int getCount() {
        return peerList.size();
    }


    public DiscoveredPeer getItem(int position) {
        return peerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        //TODO is this correct?
        return position;
    }

    @Override
    public int getItemCount() {
        return peerList.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.contact_presence_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DiscoveredPeer peer = getItem(position);
        if (peer != null) {
            //contact.setImageResource(peer.getPicture());
            int size = holder.contact.getResources().getDimensionPixelSize(R.dimen.contact_pic_size);
            //loadBitmap(peer.getPicture(), holder.contact, size, size);
            imageFetcher.loadImage(peer.getPicture(),holder.contact);
            holder.name.setText(peer.getName());
            String statusText = peer.getStatus();
            if (statusText.startsWith("Off")) {
                holder.status.setImageDrawable(null);
            } else if (statusText.equals("Busy")) {
                holder.status.setImageResource(R.drawable.status_busy);
            } else {
                holder.status.setImageResource(R.drawable.status_online);
            }
        } else {
            holder.status.setImageDrawable(null);
        }

    }

    @Override
    public int getItemViewType(int position) {
        //	as there is only one view type in the list it should always return the same view type
        return 0;
    }


//    @Override
//    public boolean isEnabled(int position) {
//        DiscoveredPeer peer = (DiscoveredPeer) getItem(position);
//        String status = peer.getStatus();
//        return status.equals("Online");
//    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView contact, status;
        public TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.contactName);
            status = (ImageView) itemView.findViewById(R.id.contactStatus);
            contact = (ImageView) itemView.findViewById(R.id.contactPic);
        }
    }

//    //For ImageLoad Testing
//    // Move all these to ImageLoaders
//    public static LruCache<Integer, Bitmap> lruCache;
//
//    private void createMemoryCache() {
//        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
//        final int cacheSize = maxMemory / 8;
//        if (lruCache == null) {
//            lruCache = new LruCache<Integer, Bitmap>(cacheSize) {
//                @Override
//                protected int sizeOf(Integer key, Bitmap bitmap) {
//                    return bitmap.getByteCount() / 1024;
//                }
//            };
//        }
//    }
//
//    public static void addBitmapToMemoryCache(Integer resId, Bitmap bitmap) {
//        if (getBitmapFromMemoryCache(resId) == null)
//            lruCache.put(resId, bitmap);
//    }
//
//    public static Bitmap getBitmapFromMemoryCache(Integer key) {
//        return lruCache.get(key);
//    }
//
//    public void loadBitmap(int resId, ImageView imageView, int desiredWidth, int desiredHeight) {
//        if(getBitmapFromMemoryCache(resId) !=null){
//            imageView.setImageBitmap(getBitmapFromMemoryCache(resId));
//            return;
//        }
//
//        if (cancelPotentialWork(resId, imageView)) {
//            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
//            final AsyncDrawable asyncDrawable = new AsyncDrawable(imageView.getResources(), null, task);
//            imageView.setImageDrawable(asyncDrawable);
//            task.execute(resId, desiredWidth, desiredHeight);
//        }
//    }
//
//    public static boolean cancelPotentialWork(int resId, ImageView imageView) {
//        BitmapWorkerTask bitmapWorkerTask = getBitmapWorkedTask(imageView);
//        if (bitmapWorkerTask != null) {
//            final int bitmapData = bitmapWorkerTask.drawableId;
//            if (bitmapData == 0 || bitmapData != resId) {
//                bitmapWorkerTask.cancel(true);
//            } else {
//                return false;
//            }
//        }
//        return true;
//
//    }
//
//    public static BitmapWorkerTask getBitmapWorkedTask(ImageView imageView) {
//        if (imageView != null) {
//            final Drawable drawable = imageView.getDrawable();
//            if (drawable instanceof AsyncDrawable) {
//                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
//                return asyncDrawable.getBitmapWorkerTask();
//            }
//        }
//        return null;
//    }

}
