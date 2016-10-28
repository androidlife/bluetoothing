package com.wordpress.laaptu.bluetooth.test.connect;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.wordpress.laaptu.bluetooth.R;
import com.wordpress.laaptu.bluetooth.test.base.DiscoveredPeer;
import com.wordpress.laaptu.bluetooth.test.base.PeerDiscoveryProvider;
import com.wordpress.laaptu.bluetooth.test.bitmaps.loaders.ImageFetcher;
import com.wordpress.laaptu.bluetooth.test.bluetooth.UserPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import timber.log.Timber;


/**
 * Created by laaptu on 10/25/16.
 */

public class OnlineFragment extends Fragment implements PeerListAdapter.OnItemClickListener {


    private RecyclerView contactList;
    private final Comparator<DiscoveredPeer> peerComparator = new Comparator<DiscoveredPeer>() {

        @Override
        public int compare(DiscoveredPeer arg0, DiscoveredPeer arg1) {
            int priorityDifference = arg0.getPriority() - arg1.getPriority();
            if (priorityDifference == 0) {
                return arg0.getName().compareTo(arg1.getName());
            }
            return priorityDifference;
        }

    };
    private PeerListAdapter peerAdapter;
    private PeerDiscoveryProvider.OnPeerDiscoveredListener peerListener;
    private PeerDiscoveryProvider discoveryProvider;
    private ArrayList<DiscoveredPeer> staticPeers;
    private ImageFetcher imageFetcher;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_online, container, false);
        contactList = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        contactList.setLayoutManager(linearLayoutManager);
        return view;
    }


    public void setProvider(PeerDiscoveryProvider discoveryProvider) {
        this.discoveryProvider = discoveryProvider;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (discoveryProvider != null) {
            discoveryProvider.setOnPeerDiscoveredListener(null);
            discoveryProvider = null;
        }
        if (contactList != null) {
            contactList.setAdapter(null);
        }
        if (peerAdapter != null) {
            peerAdapter.clear();
            peerAdapter.notifyDataSetChanged();
            peerAdapter.setOnItemClickListener(null);
            peerAdapter = null;
        }
        peerListener = null;
        if (staticPeers != null) {
            staticPeers.clear();
            staticPeers = null;
        }

        if (imageFetcher != null) {
            imageFetcher.setExitTasksEarly(true);
            imageFetcher = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        imageFetcher = new ImageFetcher(getActivity(), getResources().getDimensionPixelSize(R.dimen.contact_pic_size));
        peerAdapter = new PeerListAdapter(getActivity(), contactList, imageFetcher);
        contactList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (imageFetcher == null)
                    return;
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    imageFetcher.setPauseWork(false);
                else
                    imageFetcher.setPauseWork(true);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        UserPool.setContext(getActivity());
        staticPeers = new ArrayList<>();
        staticPeers.add(new StaticPeer(UserPool.getBusyUser(), "Busy", 3));
        UserPool.User[] offlineUsers = UserPool.getOfflineUsers();
        for (UserPool.User offlineUser : offlineUsers) {
            staticPeers.add(new StaticPeer(offlineUser, "Offline", 4));
        }

        peerAdapter.addAll(staticPeers);
        peerAdapter.setOnItemClickListener(this);

        peerListener = new PeerDiscoveryProvider.OnPeerDiscoveredListener() {
            @Override
            public void onPeersDiscovered(final Collection<DiscoveredPeer> discoveredPeers) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (DiscoveredPeer peer : discoveredPeers)
                                peerAdapter.addAt(0, peer);
                            peerAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            @Override
            public void onSinglePeerDiscovered(DiscoveredPeer discoveredPeer) {
                if (peerAdapter != null)
                    peerAdapter.addNewPeer(discoveredPeer);

            }

            @Override
            public void onPeersLost(final Collection<DiscoveredPeer> lostPeers) {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (DiscoveredPeer peer : lostPeers) {
                                peerAdapter.remove(peer);
                            }
//                            peerAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        };

        contactList.setAdapter(peerAdapter);
        //reload of discovery provider only
        if (discoveryProvider != null) {
            discoveryProvider.setOnPeerDiscoveredListener(peerListener);
            //discoveryProvider.reload();
        }
        referesh();

    }

    /**
     * On Connection functions
     */
    @Override
    public void onItemClicked(final DiscoveredPeer peer) {
        Timber.d("Clicked peer name =%s and address =%s", peer.getName(), peer.getUniqueIdentifier());
        //TODO to add
        DialogMethod dialogMethod = new DialogMethod() {
            @Override
            public void invokeMethod() {
                AsyncTask<Object, Void, Void> connectTask = new AsyncTask<Object, Void, Void>() {

                    @Override
                    protected Void doInBackground(Object... params) {
                        final DiscoveredPeer peer = (DiscoveredPeer) params[0];
                        final Fragment progress = (Fragment) params[1];
                        final Runnable dismiss = new Runnable() {

                            @Override
                            public void run() {
                                if (progress != null && progress.getFragmentManager() != null) {
                                    progress.getFragmentManager().popBackStack();
                                }
                            }

                        };
                        peer.connectTo(new DiscoveredPeer.ConnectionListener() {

                            @Override
                            public void onAccepted() {
                                OnlineFragment.this.getActivity().runOnUiThread(dismiss);
                            }

                            @Override
                            public void onDeclined() {
                                final FragmentActivity activity = OnlineFragment.this.getActivity();
                                activity.runOnUiThread(dismiss);
                                activity.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        UserBusyDialog
                                                .getInstance("User " + peer.getName() + " is currently unavailable",dialogStyle)
                                                .show(activity.getSupportFragmentManager(), "Busy");
                                    }
                                });
                            }
                        });
                        return null;
                    }

                };
                Fragment progress = ConnectingProgressFragment.create(action, connectingBackgroundId, connectTask, "Connecting", peer);
                getFragmentManager().beginTransaction().replace(R.id.container, progress).addToBackStack("Connecting").commit();
                connectTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, peer, progress);

            }
        };
        ConnectDialogFragment.getInstance(peer.getName(), dialogStyle, dialogMethod).show(getFragmentManager(), "ConnectConfirm");
    }

    public static class UserBusyDialog extends DialogFragment {
        private static final String MESSAGE = "message", DIALOG_STYLE = "dialogStyle";

        public static UserBusyDialog getInstance(String message, int dialogStyle) {
            UserBusyDialog fragment = new UserBusyDialog();
            Bundle params = new Bundle();
            params.putString(MESSAGE, message);
            params.putInt(DIALOG_STYLE, dialogStyle);
            fragment.setArguments(params);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(
                Bundle savedInstanceState) {
            Bundle params = getArguments();
            if (params == null) {
                return null;
            }
            int dialogStyle = params.getInt(DIALOG_STYLE);
            String message = params.getString(MESSAGE);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), dialogStyle);
            builder.setMessage(message).setNeutralButton("Okay", null);
            return builder.create();
        }
    }

    public static class ConnectDialogFragment extends DialogFragment {
        public ConnectDialogFragment() {
        }

        private static final String PEER_NAME = "peerName", DIALOG_STYLE = "dialogStyle", DIALOG_METHOD = "dialogMethod";

        public static ConnectDialogFragment getInstance(String peerName, int dialogStyle, DialogMethod dialogMethod) {
            ConnectDialogFragment fragment = new ConnectDialogFragment();
            Bundle params = new Bundle();
            params.putString(PEER_NAME, peerName);
            params.putInt(DIALOG_STYLE, dialogStyle);
            params.putParcelable(DIALOG_METHOD, dialogMethod);
            fragment.setArguments(params);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle params = getArguments();
            if (params == null) {
                return null;
            }
            int dialogStyle = params.getInt(DIALOG_STYLE);
            String peerName = params.getString(PEER_NAME);
            final DialogMethod dialogMethod = params.getParcelable(DIALOG_METHOD);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), dialogStyle);
            builder.setTitle("Connect to Peer?").setMessage("Open connection to " + peerName + "?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (dialogMethod != null)
                        dialogMethod.invokeMethod();
                }
            });
            builder.setNegativeButton("No", null).setInverseBackgroundForced(true);
            Dialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(true);
            return dialog;
        }
    }


    //NOT IMPORTANT RIGHT NOW
    public static class StaticPeer implements DiscoveredPeer {

        private UserPool.User user;
        private String status;
        private int priority;

        public StaticPeer(UserPool.User user, String status, int priority) {
            this.user = user;
            this.status = status;
            this.priority = priority;
        }

        @Override
        public String getName() {
            return user.name;
        }

        @Override
        public int getPicture() {
            return user.portraitid;
        }

        @Override
        public String getStatus() {
            return status;
        }

        @Override
        public void connectTo(ConnectionListener connectioListener) {
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public void release() {
        }

        @Override
        public String getUniqueIdentifier() {
            return null;
        }
    }

    //TODO Remove it later
    private int dialogStyle = android.R.style.Theme_Holo_Panel;
    private String action = "TouchTrails";
    private int connectingBackgroundId = android.R.color.black;

    public void referesh() {
//        Button button = (Button) getView().findViewById(R.id.btn_refresh);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (discoveryProvider != null)
//                    discoveryProvider.reload();
//            }
//        });

    }
}
