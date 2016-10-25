package com.wordpress.laaptu.bluetooth.test.connect;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wordpress.laaptu.bluetooth.R;
import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class PeerListAdapter extends ArrayAdapter<DiscoveredPeer> {
    private LayoutInflater layoutInflater;
    private ArrayList<DiscoveredPeer> peerList;

    public PeerListAdapter(Activity activity) {
        super(activity, 0);
        layoutInflater = LayoutInflater.from(activity);
        peerList = new ArrayList<>();
    }

    @Override
    public void addAll(Collection<? extends DiscoveredPeer> collection) {
        peerList.addAll(collection);
    }

    @Override
    public void clear() {
        peerList.clear();
    }

    @Override
    public void add(DiscoveredPeer object) {
        peerList.add(object);
    }

    @Override
    public void remove(DiscoveredPeer object) {
        peerList.remove(object);
    }

    @Override
    public void sort(Comparator<? super DiscoveredPeer> comparator) {
        Collections.sort(peerList, comparator);
    }

    public synchronized void addNewPeer(DiscoveredPeer peer) {
        remove(peer);
        peerList.add(0, peer);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return peerList.size();
    }

    @Nullable
    @Override
    public DiscoveredPeer getItem(int position) {
        return peerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        //TODO is this correct?
        return position;
    }


    @Override
    public int getItemViewType(int position) {
        //	as there is only one view type in the list it should always return the same view type
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.contact_presence_view, parent, false);
        }
        if (convertView != null) {
            DiscoveredPeer peer = (DiscoveredPeer) getItem(position);
            TextView name = (TextView) convertView.findViewById(R.id.contactName);
            ImageView status = (ImageView) convertView.findViewById(R.id.contactStatus);
            ImageView contact = (ImageView) convertView.findViewById(R.id.contactPic);
            if (peer != null) {
                contact.setImageResource(peer.getPicture());
                name.setText(peer.getName());
                String statusText = peer.getStatus();
                if (statusText.startsWith("Off")) {
                    status.setImageDrawable(null);
                } else if (statusText.equals("Busy")) {
                    status.setImageResource(R.drawable.status_busy);
                } else {
                    status.setImageResource(R.drawable.status_online);
                }
            } else {
                status.setImageDrawable(null);
            }
        }
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        // for now at least there is only one type of view in the contact list
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        DiscoveredPeer peer = (DiscoveredPeer) getItem(position);
        String status = peer.getStatus();
        return status.equals("Online");
    }

}
