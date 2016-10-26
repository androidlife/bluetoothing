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

    public PeerListAdapter(Activity activity, RecyclerView recyclerView, ImageFetcher imageFetcher) {
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
            imageFetcher.loadImage(peer.getPicture(), holder.contact);
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


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView contact, status;
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.contactName);
            status = (ImageView) itemView.findViewById(R.id.contactStatus);
            contact = (ImageView) itemView.findViewById(R.id.contactPic);
        }
    }


}
