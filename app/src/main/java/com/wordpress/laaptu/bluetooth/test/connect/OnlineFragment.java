package com.wordpress.laaptu.bluetooth.test.connect;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wordpress.laaptu.bluetooth.R;
import com.wordpress.laaptu.bluetooth.test.base.PeerDiscoveryProvider;

/**
 * Created by laaptu on 10/25/16.
 */

public class OnlineFragment extends Fragment {


    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_online, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        return view;
    }

    private PeerDiscoveryProvider discoveryProvider;

    public void setProvider(PeerDiscoveryProvider discoveryProvider) {
        this.discoveryProvider = discoveryProvider;
    }

    @Override
    public void onPause() {
        super.onPause();
        discoveryProvider = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        //reload of discovery provider only
    }
}
