package com.wordpress.laaptu.bluetooth.test.refactor.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.wordpress.laaptu.bluetooth.R;
import com.wordpress.laaptu.bluetooth.test.bitmaps.loaders.ImageFetcher;
import com.wordpress.laaptu.bluetooth.test.refactor.IntentUtils;
import com.wordpress.laaptu.bluetooth.test.refactor.UserPool;
import com.wordpress.laaptu.bluetooth.test.refactor.base.DiscoveredPeer;
import com.wordpress.laaptu.bluetooth.test.refactor.base.SocketCommunicator;
import com.wordpress.laaptu.bluetooth.test.refactor.bluetooth.BluetoothProvider;

import java.util.ArrayList;
import java.util.Collection;

import timber.log.Timber;

/**
 */

public class OnlineFragment extends Fragment implements PeerListAdapter.OnItemClickListener, SocketCommunicator.View {

    String action = IntentUtils.Extras.ACTION_TOUCHCHAT, username = UserPool.getDefaultUserName();
    private int dialogStyle;
    private int connectingBackgroundId;
    private int backgroundId = -1;
    private RecyclerView contactList;
    //private ImageFetcher imageFetcher;
    private PeerListAdapter peerAdapter;
    private ArrayList<DiscoveredPeer> staticPeers;
    private SocketCommunicator.SocketProvider socketProvider;
    private static final String FRAG_CONNECT_CONFIRM = "ConnectConfirmFrag",
            FRAG_SHOW_PROGRESS = "ProgressFrag", FRAG_USER_BUSY = "UserbusyFrag", FRAG_CONNECT_REQUEST = "ConnectRequest";
    private boolean test = false;

    private String dialogType = null;

    //All Implemented interface starts here


    /**
     * Provider interface
     * ...starts
     */
    @Override
    public void start() {
        startUI();
        startProvider();
    }

    private void startProvider() {
        socketProvider = new BluetoothProvider(getActivity(), this, action, username);
        socketProvider.start();
    }

    @Override
    public void stop() {
        stopUI();
        stopProvider();

    }

    private void stopProvider() {
        if (socketProvider != null) {
            socketProvider.stop();
            socketProvider = null;
        }
    }

    /**
     * PeerDiscoveryProvider.OnPeerDiscoveredListener interface
     * ...starts
     * will be called from PeerProvider implemented class
     */
    @Override
    public void onPeersDiscovered(Collection<DiscoveredPeer> discoveredPeers) {
        if (peerAdapter != null) {
            for (DiscoveredPeer peer : discoveredPeers)
                peerAdapter.addAt(0, peer);
            peerAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onSinglePeerDiscovered(DiscoveredPeer discoveredPeer) {
        if (peerAdapter != null)
            peerAdapter.addNewPeer(discoveredPeer);

    }

    @Override
    public void onPeersLost(Collection<DiscoveredPeer> lostPeers) {
        if (peerAdapter != null) {
            for (DiscoveredPeer peer : lostPeers) {
                peerAdapter.remove(peer);
            }
        }

    }

    /**
     * ViewProvider interface
     * ...starts
     * will be called from ClientServer
     */
    private DiscoveredPeer connectionRequestedPeer;

    //this is server
    @Override
    public void connectFrom(DiscoveredPeer peer) {
        connectionRequestedPeer = peer;
        //show dialog
        //this is called from somethread, so it needs
        // to be on main thread
        //TODO remove any fragments, before being added
        RequestDialog.DialogMethod dialogMethod = new RequestDialog.DialogMethod() {
            @Override
            public void acceptReject(boolean accept) {
                resetDialogType(true);
                if (OnlineFragment.this.socketProvider != null) {
                    socketProvider.yesNoMsg(accept);
                    if (accept) {
                        //TODO show ConnectingProgressFragment
                    }
                }
            }
        };
        if (test) {
            dialogMethod.acceptReject(true);
            return;
        }
        String title = "Confirm Connection";
        String message = "Okay to connect " + connectionRequestedPeer.getName() + "?";
        RequestDialog.getInstance(title, message, dialogStyle, false, dialogMethod)
                .show(getFragmentManager(), FRAG_CONNECT_REQUEST);
        setDialogType(FRAG_CONNECT_REQUEST);
    }


    //this is client
    @Override
    public void onItemClicked(DiscoveredPeer peer) {
        connectionRequestedPeer = peer;
        //show dialog
        //TODO remove any dialog before adding them
        // look for DialogFragment implementation first
        RequestDialog.DialogMethod dialogMethod = new RequestDialog.DialogMethod() {
            //called after dialog is dismissed
            @Override
            public void acceptReject(boolean accept) {
                resetDialogType(true);
                if (accept && OnlineFragment.this.socketProvider != null) {
                    showConnectingProgress();
                    socketProvider.connectTo(connectionRequestedPeer);
                }
            }
        };
        if (test) {
            dialogMethod.acceptReject(true);
            return;
        }
        String title = "Connect to Peer?";
        String message = "Open connection to " + connectionRequestedPeer.getName() + "?";
        RequestDialog.getInstance(title, message, dialogStyle, false, dialogMethod)
                .show(getFragmentManager(), FRAG_CONNECT_CONFIRM);
        setDialogType(FRAG_CONNECT_CONFIRM);

    }

    private void showConnectingProgress() {
        RequestDialog.DialogMethod dialogMethod = new RequestDialog.DialogMethod() {
            @Override
            public void acceptReject(boolean accept) {
                resetDialogType(accept);
            }
        };
        ConnectingProgressFragment.create(action,
                connectingBackgroundId, "Connecting", connectionRequestedPeer.getName(),
                connectionRequestedPeer.getPicture(), dialogMethod)
                .show(getFragmentManager(), FRAG_SHOW_PROGRESS);
        setDialogType(FRAG_SHOW_PROGRESS);
    }


    private void setDialogType(String dialogType) {
        this.dialogType = dialogType;
    }

    private void resetDialogType(boolean normalDismiss) {
        /**
         * This is the case where during progress, the user cancels the progress
         * dialog by backpress or by stopping
         * and in that case if there is connection request
         * to server, cancel that as well*/
        Timber.d("Dialog cancellation normal =%b for dialogtype =%s", normalDismiss, dialogType);
        if (dialogType.equals(FRAG_SHOW_PROGRESS) && !normalDismiss && socketProvider != null) {
            socketProvider.cancelClientConnectionRequest();
        }
        setDialogType(null);
    }


    @Override
    public void acceptReject(final boolean accept) {
        //this should be run on ui thread

        //if it is from client
        //removing progress fragment
        Fragment prev = getFragmentManager().findFragmentByTag(FRAG_SHOW_PROGRESS);
        if (prev != null) {
            getFragmentManager().beginTransaction().remove(prev).commit();
            setDialogType(null);
            if (!accept) {
                RequestDialog.getInstance(null, "User " + connectionRequestedPeer.getName() +
                        " is currently unavailable", dialogStyle, true, null)
                        .show(getFragmentManager(), FRAG_USER_BUSY);
                setDialogType(FRAG_USER_BUSY);
            }
        }
        //show user busy dialog


        if (accept) {
            //navigate to some activity
            //connection accepted
            //TODO move this to separate IntentUtils
            //Intent intent =new Intent
            stopProvider();
            String[] params = {action, IntentUtils.Extras.MEDIUM_BLUETOOTH,
                    connectionRequestedPeer.getUniqueIdentifier(), connectionRequestedPeer.getName()};
            IntentUtils.navigateToChat(getActivity(), connectionRequestedPeer.isServer(), params);
        }


    }


    /**
     * Connection Lost
     */
    @Override
    public void connectionLost() {
        stop();
        getActivity().finish();
    }

    //-------------------------------------------------------


    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume()");
        start();
    }

    private void startUI() {
        //imageFetcher = new ImageFetcher(getActivity(), getResources().getDimensionPixelSize(R.dimen.contact_pic_size));
        Picasso.with(getActivity()).resumeTag(getActivity());
        contactList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                if (imageFetcher == null)
//                    return;
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //imageFetcher.setPauseWork(false);
                    Picasso.with(getActivity()).resumeTag(getActivity());
                } else {
                    //imageFetcher.setPauseWork(true);
                    Picasso.with(getActivity()).pauseTag(getActivity());
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        peerAdapter = new PeerListAdapter(getActivity(), contactList);
        UserPool.setContext(getActivity());
        staticPeers = new ArrayList<>();
        staticPeers.add(new UserPool.StaticPeer(UserPool.getBusyUser(), "Busy", 3));
        UserPool.User[] offlineUsers = UserPool.getOfflineUsers();
        for (UserPool.User offlineUser : offlineUsers) {
            staticPeers.add(new UserPool.StaticPeer(offlineUser, "Offline", 4));
        }

        peerAdapter.addAll(staticPeers);
        peerAdapter.setOnItemClickListener(this);
        contactList.setAdapter(peerAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause()");
        stop();

    }

    private void stopUI() {
        if (peerAdapter != null) {
            peerAdapter.clearAll();
            peerAdapter = null;
        }
        if (contactList != null) {
            contactList.setAdapter(null);
        }

        if (staticPeers != null) {
            staticPeers.clear();
            staticPeers = null;
        }
        Picasso.with(getActivity()).pauseTag(getActivity());

//        if (imageFetcher != null) {
//            imageFetcher.setExitTasksEarly(true);
//            imageFetcher.clearCache();
//            imageFetcher = null;
//        }
    }


    /**
     * All view related stuff
     * background,layouts
     */
    public OnlineFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_online, container, false);
        contactList = (RecyclerView) view.findViewById(R.id.contacts_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        contactList.setLayoutManager(linearLayoutManager);
        return view;
    }

    public void passIntent(Intent intent) {
        if (intent == null)
            return;
        String action = intent.getStringExtra(IntentUtils.Extras.ACTION);
        if (TextUtils.isEmpty(action))
            action = IntentUtils.Extras.ACTION_TOUCHCHAT;
        setThemeAsPerAction(action);
        username = intent.getStringExtra(IntentUtils.Extras.USERNAME);
    }


    private void setThemeAsPerAction(String action) {
        this.action = action;
        boolean isDark = false;
        if (IntentUtils.Extras.ACTION_TOUCHTRAILS.equals(action)) {
            backgroundId = R.color.black;
            connectingBackgroundId = R.color.black;
            isDark = true;
        } else if (IntentUtils.Extras.ACTION_TOUCHVIDEO.equals(action)) {
            backgroundId = R.drawable.bg_video_connecting;
            connectingBackgroundId = R.drawable.bg_video_connecting_progress;
        } else if (IntentUtils.Extras.ACTION_TOUCHDICE.equals(action)) {
            backgroundId = R.drawable.bg_dice_connecting;
            connectingBackgroundId = R.drawable.bg_dice_connecting;
            isDark = true;
        } else if (IntentUtils.Extras.ACTION_TOUCHCHAT.equals(action)) {
            backgroundId = R.drawable.bg_chat_connecting;
            connectingBackgroundId = R.drawable.bg_chat_connecting_progress;
        }
        if (isDark) {
            dialogStyle = android.R.style.Theme_Holo_Panel;
        } else {
            dialogStyle = android.R.style.Theme_Holo_Light_Panel;
        }
    }


}
