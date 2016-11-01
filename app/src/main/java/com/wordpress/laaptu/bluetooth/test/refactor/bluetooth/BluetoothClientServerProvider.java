package com.wordpress.laaptu.bluetooth.test.refactor.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
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

    /**
     * This is done to know the state of this class
     * right now only two states
     * STATE_PROCESS, all the tasks, before accept reject call
     * STATE_ACCEPT_REJECT, accept,reject called from sendMessageThread
     * This is done as when accept/reject is called we need
     * to cancel some threads
     * reject: Send Msg Thread and ConnectToServer Thread
     * accept: Send Msg Thread + ConnectToServer Thread+ Server Thread
     * While cancel, we need to close the sockets and it throws
     * error and this error has callback. So during ACCEPT_REJECT state
     * we don't need to give callback
     */
//    public static class State {
//        static final int STATE_PROCESS = 0x1, STATE_ACCEPT_REJECT_MAIN = 0x2;
//    }

    //private int state = State.STATE_PROCESS;


    public static class HandlerMsg {
        static final int SERVER_CREATION_ERROR = 0x1;
        static final int SERVER_REJECT_INCOMING_CONNECTION = 0x2;
        static final int SERVER_ACCEPT_INCOMING_CONNECTION = 0x3;
        static final int CLIENT_START_ERROR = 0x4;
        static final int CLIENT_CONNECT_TO_SERVER_ERROR = 0x5;
        static final int CLIENT_CONNECT_TO_SERVER_SUCCESS = 0x6;
        static final int CLIENT_UNAME_RECEIVED = 0x7;
        static final int SEND_MSG_STREAM_CREATE_ERROR = 0x8;
        static final int SEND_MSG_IP_STREAM_ERROR = 0x9;
        static final int SEND_MSG_OP_STREAM_ERROR = 0x10;
        static final int SEND_MSG_ACCEPT_REJECT_STATUS = 0x11;
    }

    //TODO make this a static class
    private Handler mainUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HandlerMsg.SERVER_CREATION_ERROR:
                    //Log error only, can't do anything more
                    sendError(Error.ERROR_SERVER_CREATION);
                    break;
                case HandlerMsg.SERVER_REJECT_INCOMING_CONNECTION:
                    Timber.d("Server reject incoming and state =%d", state);
                    //if (state == State.STATE_PROCESS)
                    acceptReject(false);
                    break;
                case HandlerMsg.SERVER_ACCEPT_INCOMING_CONNECTION:
                    deliverTheSocket((BluetoothSocket) msg.obj, true);
                    break;
                case HandlerMsg.CLIENT_START_ERROR:
                case HandlerMsg.CLIENT_CONNECT_TO_SERVER_ERROR:
                    sendError(Error.ERROR_CONNECT_SERVER_SOCKET);
                    stopConnectToServerThread();
                    Timber.d("Client couldn't connect to server =%d", state);
                    //if (state == State.STATE_PROCESS)
                    acceptReject(false);
                    break;
                case HandlerMsg.CLIENT_CONNECT_TO_SERVER_SUCCESS:
                    deliverTheSocket((BluetoothSocket) msg.obj, false);
                    break;
                case HandlerMsg.SEND_MSG_STREAM_CREATE_ERROR:
                case HandlerMsg.SEND_MSG_IP_STREAM_ERROR:
                case HandlerMsg.SEND_MSG_OP_STREAM_ERROR:
                    stopConnectToServerNSendMsgThread();
                    Timber.d("Send Message thread ip op error =%d", state);
                    //if (state == State.STATE_PROCESS)
                    acceptReject(false);
                    break;
                case HandlerMsg.CLIENT_UNAME_RECEIVED:
                    connectFrom((String) msg.obj);
                    break;
                case HandlerMsg.SEND_MSG_ACCEPT_REJECT_STATUS:
                    //state = State.STATE_ACCEPT_REJECT_MAIN;
                    stopConnectToServerNSendMsgThread();
                    boolean accepted = (boolean) msg.obj;
                    acceptReject(accepted);
                    break;
            }

        }
    };


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
        mainUiHandler = null;
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
        if (sendMessageThread != null && sendMessageThread.isRunning) {
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
        private boolean isRunning = true;

        public CreateServerAndListenForClientSocketThread(String serverName, UUID serverId) {
            BluetoothServerSocket tmp = null;
            try {
                tmp = BluetoothAdapter.getDefaultAdapter().listenUsingInsecureRfcommWithServiceRecord(serverName, serverId);
            } catch (Exception e) {
                Timber.d("Unable to create a server ");
                e.printStackTrace();
                //BluetoothClientServerProvider.this.sendError(BluetoothClientServerProvider.Error.ERROR_SERVER_CREATION);
                if (mainUiHandler != null)
                    mainUiHandler.sendEmptyMessage(HandlerMsg.SERVER_CREATION_ERROR);
            }
            if (tmp != null) Timber.d("Server created successfully");
            serverSocket = tmp;
        }

        @Override
        public void run() {
            super.run();
            BluetoothSocket socket = null;
            while (isRunning) {
                try {
                    Timber.d("Server is running successfully");
                    socket = serverSocket.accept();
                    Timber.d("Socket created successfully i.e. someone is requesting for connection");
                } catch (Exception e) {
                    Timber.e("This server reject connection, no socket available to this server");
                    e.printStackTrace();
                    //BluetoothClientServerProvider.this.acceptReject(false);
                    //don't send any error log during cancellation
                    if (mainUiHandler != null && isRunning)
                        mainUiHandler.sendEmptyMessage(HandlerMsg.SERVER_REJECT_INCOMING_CONNECTION);
                    return;
                }
                if (socket != null) {
                    // if denied, do nothing
                    // else pass the socket and close this
                    Timber.d("This is server now");
                    //BluetoothClientServerProvider.this.deliverTheSocket(socket, true);
                    if (mainUiHandler != null && isRunning) {
                        Message message = Message.obtain();
                        message.what = HandlerMsg.SERVER_ACCEPT_INCOMING_CONNECTION;
                        message.obj = socket;
                        mainUiHandler.sendMessage(message);
                    }
                    //right now not close the server
                    //cancel();
                }
                //seems like this is not essential commenting right now
//                else {
//                    BluetoothClientServerProvider.this.acceptReject(false);
//                }
            }
        }

        public void cancel() {
            isRunning = false;
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class ConnectToServerThread extends Thread {
        private BluetoothSocket socketTobePassedToServer;
        private boolean isRunning = true;

        public ConnectToServerThread(BluetoothDevice device, UUID serverId) {
            BluetoothSocket tmp = null;
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(serverId);
            } catch (Exception e) {
                Timber.e(
                        "Cannot create socket that needs to be passed to the server, going for fallback socket");
                terminateWithErrorLog(HandlerMsg.CLIENT_START_ERROR);
            }
            if (tmp != null) Timber.d("Created socket to be passed to server");
            socketTobePassedToServer = tmp;
        }

        private void terminateWithErrorLog(int what) {
//            BluetoothClientServerProvider.this.sendError(BluetoothClientServerProvider.Error.ERROR_CONNECT_SERVER_SOCKET);
//            BluetoothClientServerProvider.this.acceptReject(false);
//            BluetoothClientServerProvider.this.stopConnectToServerThread();
            if (mainUiHandler != null)
                mainUiHandler.sendEmptyMessage(what);

        }

        @Override
        public void run() {
            super.run();
            try {
                //here this socket is now trying to connect to the server
                socketTobePassedToServer.connect();
            } catch (Exception e) {
                Timber.e("Server rejected connection");
                if (isRunning)
                    terminateWithErrorLog(HandlerMsg.CLIENT_CONNECT_TO_SERVER_ERROR);
                return;
            }
            Timber.d("Connected to server,this is client now");
            //BluetoothClientServerProvider.this.deliverTheSocket(socketTobePassedToServer, false);
            if (mainUiHandler != null && isRunning) {
                Message message = Message.obtain();
                message.what = HandlerMsg.CLIENT_CONNECT_TO_SERVER_SUCCESS;
                message.obj = socketTobePassedToServer;
                mainUiHandler.sendMessage(message);
            }

        }

        public void cancel() {
            isRunning = false;
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
     * Here order of cancellation is important
     * First send msg thread must be closed
     * as it contains the connected socket
     * and then only connect to server thread
     * If connectToServer thread is closed first, socket will
     * be closed and it throws error on sendMessage thread
     */
    private void stopConnectToServerNSendMsgThread() {
        stopSendMessageThread();
        stopConnectToServerThread();
    }

    private class SendMessageThread extends Thread {
        private boolean isServer;
        private InputStream inputStream;
        private OutputStream outputStream;
        private boolean isRunning = true;
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
                terminateWithErrorLog(HandlerMsg.SEND_MSG_STREAM_CREATE_ERROR);
                return;
            }
            inputStream = inputStreamTmp;
            outputStream = outputStreamTmp;
            this.isServer = isServer;
        }

        private void terminateWithErrorLog(int what) {
//            BluetoothClientServerProvider.this.acceptReject(false);
//            stopConnectToServerNSendMsgThread();
            if (mainUiHandler != null && isRunning) {
                mainUiHandler.sendEmptyMessage(what);
            }
        }

        @Override
        public void run() {
            super.run();
            byte[] buffer = new byte[1024];
            int bytes;
            while (isRunning) {
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
                        //BluetoothClientServerProvider.this.connectFrom(msg);
                        if (mainUiHandler != null) {
                            Message message = Message.obtain();
                            message.obj = msg;
                            message.what = HandlerMsg.CLIENT_UNAME_RECEIVED;
                            mainUiHandler.sendMessage(message);
                        }

                    } else if (msg.equals(REQUEST_ACCEPT) || msg.equals(REQUEST_REJECT)) {
                        //this is client
                        // iit is now getting yes/no message from
                        //server
                        acceptReject(msg.equals(REQUEST_ACCEPT));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    terminateWithErrorLog(HandlerMsg.SEND_MSG_IP_STREAM_ERROR);
                }
            }
        }

        public void write(String msg) {
            try {
                outputStream.write(msg.getBytes());
                if (msg.equals(REQUEST_ACCEPT) || msg.equals(REQUEST_REJECT)) {
                    acceptReject(msg.equals(REQUEST_ACCEPT));
                }
            } catch (Exception e) {
                e.printStackTrace();
                terminateWithErrorLog(HandlerMsg.SEND_MSG_OP_STREAM_ERROR);
            }
        }

        private void acceptReject(boolean accept) {
            if (mainUiHandler != null && isRunning) {
                Message message = Message.obtain();
                message.what = HandlerMsg.SEND_MSG_ACCEPT_REJECT_STATUS;
                message.obj = accept;
                mainUiHandler.sendMessage(message);
            }
//            BluetoothClientServerProvider.this.acceptReject(accept);
//            if (!accept)
//                stopConnectToServerNSendMsgThread();

        }

        public void cancel() {
            isRunning = false;
            try {
                outputStream.close();
                inputStream.close();
                socket.close();
            } catch (Exception e) {
                Timber.e("Error closing socket,input stream and output stream of sendMessageThread");
            }
        }

    }

}
