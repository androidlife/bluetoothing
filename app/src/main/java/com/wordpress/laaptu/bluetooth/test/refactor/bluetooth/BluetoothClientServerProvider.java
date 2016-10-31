package com.wordpress.laaptu.bluetooth.test.refactor.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.text.TextUtils;

import com.wordpress.laaptu.bluetooth.test.refactor.IntentUtils;
import com.wordpress.laaptu.bluetooth.test.refactor.base.DiscoveredPeer;
import com.wordpress.laaptu.bluetooth.test.refactor.base.SocketCommunicator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import timber.log.Timber;

/**
 */

public class BluetoothClientServerProvider implements SocketCommunicator.ClientServerProvider {

    private SocketCommunicator.ViewProvider viewProvider;
    //need to pass these through intent as well
    private CreateServerAndListenForClientSocketThread serverThread;
    private ConnectToServerThread connectToServerThread;
    private SendMessageThread sendMessageThread;
    private String userName;
    private boolean isServer;
    private String address;
    static final String REQUEST_ACCEPT = "accept",
            REQUEST_REJECT = "reject", REQUEST_USERNAME = "username", RECEIVE_USERNAME = "receiveUname", DELIMITER = ",";

    public static class Error {
        static final int ERROR_SERVER_CREATION = 0x1, ERROR_CONNECT_SERVER_SOCKET = 0x2;
    }


    public BluetoothClientServerProvider(SocketCommunicator.ViewProvider viewProvider, String username) {
        this.viewProvider = viewProvider;
        Timber.d("BluetoothClientServer Provider , is viewprovider null =%b", viewProvider == null);
        //this null check is essential
        // as we require to pass this username
        // and if this is null, socket will be close
        this.userName = TextUtils.isEmpty(username) ? "User" : username;
    }

    @Override
    public void start() {
        serverThread = new CreateServerAndListenForClientSocketThread(IntentUtils.ServerInfo.SERVER_NAME,
                IntentUtils.ServerInfo.SERVER_UUID);
        serverThread.start();
    }

    @Override
    public void stop() {
        viewProvider = null;
        try {
            if (serverThread != null) {
                serverThread.cancel();
                serverThread.join(250);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            serverThread = null;
        }
        stopConnectToServerNSendMsgThread();
    }

    @Override
    public void connectTo(DiscoveredPeer peer) {
        stopConnectToServerThread();
        connectToServerThread = new ConnectToServerThread(BluetoothAdapter.getDefaultAdapter()
                .getRemoteDevice(peer.getUniqueIdentifier()), IntentUtils.ServerInfo.SERVER_UUID);
        connectToServerThread.start();
    }

    @Override
    public void yesNoMsg(boolean yes) {
        if (sendMessageThread != null && sendMessageThread.read) {
            sendMessageThread.write(yes ? REQUEST_ACCEPT : REQUEST_REJECT);
        }
    }

    private void acceptReject(boolean accept) {
        Timber.d("Is connection accept =%b and is view provider null = %b", accept, viewProvider == null);
        if (viewProvider != null)
            viewProvider.acceptReject(accept);
    }

    private void deliverTheSocket(BluetoothSocket bluetoothSocket, boolean isServer) {
        this.isServer = isServer;
        address = bluetoothSocket.getRemoteDevice().getAddress();
        //stopSendMessageThread();
        sendMessageThread = new SendMessageThread(bluetoothSocket, isServer);
        sendMessageThread.start();
        if (isServer)
            sendMessageThread.write(REQUEST_USERNAME);

    }

    private void connectFrom(String peerName) {
        if (viewProvider != null) {
            //this is little bit confusing
            //though this is the request coming from another peer
            //this is server
            DiscoveredPeer peer = new BluetoothPeer(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address),
                    peerName, true);
            viewProvider.connectFrom(peer);
        }

    }


    private void sendError(int errorCode) {
        //TODO make this error more readable
        Timber.e("Error code = %d", errorCode);
    }

    //All the threads out here
    //Thread stop calls
    // this is called from running thread
    // i.e. thread is stopping itself
    private void stopConnectToServerThread() {
        try {
            if (connectToServerThread != null) {
                connectToServerThread.cancel();
                connectToServerThread.join(250);
            }
        } catch (Exception e) {
            Timber.e("Error stopping connectToServerThread");
        } finally {
            connectToServerThread = null;
        }
    }

    private void stopSendMessageThread() {
        try {
            if (sendMessageThread != null) {
                sendMessageThread.cancel();
                sendMessageThread.join(250);
            }
        } catch (Exception e) {
            Timber.e("Error stopping sendMessageThread");
        } finally {
            sendMessageThread = null;
        }
    }

    private class CreateServerAndListenForClientSocketThread extends Thread {
        private final BluetoothServerSocket serverSocket;
        private boolean run = true;

        public CreateServerAndListenForClientSocketThread(String serverName, UUID serverId) {
            BluetoothServerSocket tmp = null;
            try {
                tmp = BluetoothAdapter.getDefaultAdapter().listenUsingInsecureRfcommWithServiceRecord(serverName, serverId);
            } catch (Exception e) {
                Timber.d("Unable to create a server ");
                e.printStackTrace();
                BluetoothClientServerProvider.this.sendError(BluetoothClientServerProvider.Error.ERROR_SERVER_CREATION);
            }
            if (tmp != null) Timber.d("Server created successfully");
            serverSocket = tmp;
        }

        @Override
        public void run() {
            super.run();
            BluetoothSocket socket = null;
            while (run) {
                try {
                    Timber.d("Server is running successfully");
                    socket = serverSocket.accept();
                    Timber.d("Socket created successfully i.e. someone is requesting for connection");
                } catch (Exception e) {
                    Timber.e("This server reject connection, no socket available to this server");
                    e.printStackTrace();
                    BluetoothClientServerProvider.this.acceptReject(false);
                    return;
                }
                if (socket != null) {
                    //connectedIncomingSocketToOurServer(socket);
                    //TODO create a dialog or something for us to accept the communication
                    // if denied, do nothing
                    // else pass the socket and close this
                    Timber.d("This is server now");
                    BluetoothClientServerProvider.this.deliverTheSocket(socket, true);
                    //right now not close the server
                    //cancel();
                } else {
                    BluetoothClientServerProvider.this.acceptReject(false);
                }
            }
        }

        public void cancel() {
            run = false;
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class ConnectToServerThread extends Thread {
        private BluetoothSocket socketTobePassedToServer;

        public ConnectToServerThread(BluetoothDevice device, UUID serverId) {
            BluetoothSocket tmp = null;
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(serverId);
            } catch (Exception e) {
                Timber.e(
                        "Cannot create socket that needs to be passed to the server, going for fallback socket");
                terminateWithErrorLog();
            }
            if (tmp != null) Timber.d("Created socket to be passed to server");
            socketTobePassedToServer = tmp;
        }

        private void terminateWithErrorLog() {
            BluetoothClientServerProvider.this.sendError(BluetoothClientServerProvider.Error.ERROR_CONNECT_SERVER_SOCKET);
            BluetoothClientServerProvider.this.acceptReject(false);
            BluetoothClientServerProvider.this.stopConnectToServerThread();
        }

        @Override
        public void run() {
            super.run();
            try {
                //here this socket is now trying to connect to the server
                socketTobePassedToServer.connect();
            } catch (Exception e) {
                Timber.e("Server rejected connection");
                terminateWithErrorLog();
                return;
            }
            Timber.d("Connected to server,this is client now");
            BluetoothClientServerProvider.this.deliverTheSocket(socketTobePassedToServer, false);

        }

        public void cancel() {
            try {
                socketTobePassedToServer.close();
            } catch (IOException e) {
                Timber.e("socketToBePassedToServer close error");
            }
        }

    }

    /**
     * Called from send message thread to stop all.
     * This is just a mechanism to indicate that
     * all action of SendMessageThread has been complete
     * by successful action completion or by error
     */
    private void stopConnectToServerNSendMsgThread() {
        stopConnectToServerThread();
        stopSendMessageThread();
    }

    private class SendMessageThread extends Thread {
        private boolean isServer;
        private InputStream inputStream;
        private OutputStream outputStream;
        private boolean read = true;
        private BluetoothSocket socket;

        public SendMessageThread(BluetoothSocket socket, boolean isServer) {
            this.socket = socket;
            InputStream inputStreamTmp = null;
            OutputStream outputStreamTmp = null;
            try {
                inputStreamTmp = socket.getInputStream();
                Timber.d("Input Stream accessed of connected socket");
                outputStreamTmp = socket.getOutputStream();
                Timber.d("Output stream accessed of connected socket");
            } catch (IOException e) {
                Timber.e("Unable to access input or output stream from connected socket.Closing it now");
                terminateWithErrorLog();
                return;
            }
            inputStream = inputStreamTmp;
            outputStream = outputStreamTmp;
            this.isServer = isServer;
        }

        private void terminateWithErrorLog() {
            BluetoothClientServerProvider.this.acceptReject(false);
            stopConnectToServerNSendMsgThread();
        }

        @Override
        public void run() {
            super.run();
            byte[] buffer = new byte[1024];
            int bytes;
            while (read) {
                try {
                    bytes = inputStream.read(buffer);
                    String msg = new String(buffer, 0, bytes);
                    if (msg.contains(REQUEST_USERNAME)) {
                        //server requesting username to show dialog
                        //this is client
                        write(RECEIVE_USERNAME.concat(DELIMITER).concat(BluetoothClientServerProvider.this.userName));
                    } else if (msg.contains(RECEIVE_USERNAME)) {
                        //client now sent the username
                        // this is server,show dialog now
                        msg = msg.substring(RECEIVE_USERNAME.length() + 1);
                        BluetoothClientServerProvider.this.connectFrom(msg);
                    } else if (msg.equals(REQUEST_ACCEPT) || msg.equals(REQUEST_REJECT)) {
                        //this is client
                        // iit is now getting yes/no message from
                        //server
                        acceptReject(msg.equals(REQUEST_ACCEPT));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    terminateWithErrorLog();
                }
            }
        }

        public void write(String msg) {
            try {
                outputStream.write(msg.getBytes());
                if (msg.equals(REQUEST_ACCEPT) || msg.equals(REQUEST_REJECT))
                    acceptReject(msg.equals(REQUEST_ACCEPT));
            } catch (Exception e) {
                e.printStackTrace();
                terminateWithErrorLog();
            }
        }

        private void acceptReject(boolean accept) {
            BluetoothClientServerProvider.this.acceptReject(accept);
            if (!accept)
                stopConnectToServerNSendMsgThread();

        }

        public void cancel() {
            try {
                read = false;
                outputStream.close();
                inputStream.close();
                socket.close();
            } catch (Exception e) {
                Timber.e("Error closing socket,input stream and output stream of sendMessageThread");
            }
        }

    }

}
