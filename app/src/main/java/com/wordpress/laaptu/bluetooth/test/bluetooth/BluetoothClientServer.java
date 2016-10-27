package com.wordpress.laaptu.bluetooth.test.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import com.wordpress.laaptu.bluetooth.test.socket.SocketProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.util.UUID;

import timber.log.Timber;

public class BluetoothClientServer {
    static final String SERVER_NAME = "LiveTouchChatServer";
    static final UUID SERVER_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a67");
    static final String MSG_REQUEST_CONNECT = "requestConnect", REQUEST_ACCEPT = "accept",
            REQUEST_REJECT = "reject";

    public static class Error {
        static final int ERROR_SERVER_CREATION = 0x1, ERROR_CONNECT_SERVER_SOCKET = 0x2;
    }

    public interface ErrorEncountered {
        void onError(int errorCode);
    }

    private CreateServerAndListenForClientSocketThread serverThread;
    private ConnectToServerThread connectToServerThread;
    private OnClientServerListener clientServerListener;
    private SendMessageThread sendMessageThread;

    public void start(OnClientServerListener clientServerListener) {
        this.clientServerListener = clientServerListener;
        serverThread = new CreateServerAndListenForClientSocketThread();
        serverThread.start();
    }


    public void connectTo(BluetoothDevice bluetoothDevice) {
        stopConnectToServerThread();
        connectToServerThread = new ConnectToServerThread(bluetoothDevice);
        connectToServerThread.start();

    }

    private void stopConnectToServerThread() {
        try {
            if (connectToServerThread != null) {
                connectToServerThread.cancel();
                connectToServerThread.join(250);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectToServerThread = null;
        }
    }

    public void stop() {
        clientServerListener = null;
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
        stopConnectToServerThread();
        stopSendMessageThread();
    }

    private void stopSendMessageThread() {
        try {
            if (sendMessageThread != null) {
                sendMessageThread.cancel();
                sendMessageThread.join(250);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sendMessageThread = null;
        }
    }

    private void sendError(int errorCode) {
        if (clientServerListener != null)
            clientServerListener.onError(errorCode);
    }

    private boolean isServer;

    private void deliverTheSocket(BluetoothSocket bluetoothSocket, boolean isServer) {
        this.isServer = isServer;
        SocketProvider.getInstance().address = bluetoothSocket.getRemoteDevice().getAddress();
        SocketProvider.getInstance().isServer = isServer;
        stopSendMessageThread();
        sendMessageThread = new SendMessageThread(bluetoothSocket, isServer);
        sendMessageThread.start();
        // need to create a dialog
        // need to save the peer name as well
        if (isServer) {
            clientServerListener.showDialog("Some Peer ", new OnClientServerListener.OnRequestListener() {
                @Override
                public void onRequestAccepted(boolean accept) {
                    sendMessageThread.write(accept ? REQUEST_ACCEPT.getBytes() : REQUEST_REJECT.getBytes());
                    onConnectionSuccess(accept);
                }
            });
        }

    }

    private void rejectConnection() {
        if (clientServerListener != null)
            clientServerListener.onConnectionReject();
    }

    private void pauseDiscovery(boolean pause) {
        if (clientServerListener != null)
            clientServerListener.pauseDiscovery(pause);
    }

    //for client only
    private void onMessageReceived(String message, boolean isServer) {
        //this is for client only
        stopSendMessageThread();
        if (!isServer) {
            onConnectionSuccess(message.equals(REQUEST_ACCEPT));
        }
    }

    //common to both server and client
    private void onConnectionSuccess(boolean success) {
        stopSendMessageThread();
        if (success) {
            stopConnectToServerThread();
            if (clientServerListener != null)
                clientServerListener.onConnectionAccept(null);
        } else {

            rejectConnection();
        }
    }

    /**
     * Thread portion
     */

    private class CreateServerAndListenForClientSocketThread extends Thread {
        private final BluetoothServerSocket serverSocket;
        private boolean run = true;

        public CreateServerAndListenForClientSocketThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = BluetoothAdapter.getDefaultAdapter().listenUsingInsecureRfcommWithServiceRecord(SERVER_NAME, SERVER_UUID);
            } catch (Exception e) {
                Timber.d("Unable to create a server ");
                e.printStackTrace();
                BluetoothClientServer.this.sendError(Error.ERROR_SERVER_CREATION);
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
                    Timber.e("Socket creation problem");
                    e.printStackTrace();
                    BluetoothClientServer.this.rejectConnection();
                    return;
                }
                if (socket != null) {
                    //connectedIncomingSocketToOurServer(socket);
                    //TODO create a dialog or something for us to accept the communication
                    // if denied, do nothing
                    // else pass the socket and close this
                    Timber.d("This is server now");
                    BluetoothClientServer.this.deliverTheSocket(socket, true);
                    //right now not close the server
                    //cancel();
                } else {
                    BluetoothClientServer.this.rejectConnection();
                }
            }
        }

        private void closeSocket() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            closeSocket();
            run = false;
        }
    }

    private class ConnectToServerThread extends Thread {
        private BluetoothSocket socketTobePassedToServer;
        private final BluetoothDevice serverBluetoothDevice;

        public ConnectToServerThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            serverBluetoothDevice = device;
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(SERVER_UUID);
            } catch (Exception e) {
                Timber.e(
                        "Cannot create socket that needs to be passed to the server, going for fallback socket");
                e.printStackTrace();
                BluetoothClientServer.this.sendError(Error.ERROR_CONNECT_SERVER_SOCKET);
                BluetoothClientServer.this.rejectConnection();
            }
            if (tmp != null) Timber.d("Created socket to be passed to server without reflection");
            socketTobePassedToServer = tmp;
        }

        @Override
        public void run() {
            super.run();
            //cancel discovery as startDiscovery() by itself is resource intensive
            //bluetoothAdapter.cancelDiscovery();
            BluetoothClientServer.this.pauseDiscovery(true);
            try {
                //here this socket is now trying to connect to the server
                socketTobePassedToServer.connect();
            } catch (Exception e) {
                Timber.e("Cannot connect to the server,using fallback socket");
                e.printStackTrace();
                cancel();
                BluetoothClientServer.this.rejectConnection();
                return;
            }
            //connectedToServerOurSocket(socketTobePassedToServer);
            Timber.d("This is client now");
            BluetoothClientServer.this.deliverTheSocket(socketTobePassedToServer, false);

        }

        public void cancel() {
//            try {
//                socketTobePassedToServer.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
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
                Timber.d("Input Stream accessed of socket");
                outputStreamTmp = socket.getOutputStream();
                Timber.d("Output stream accessed of socket");
            } catch (IOException e) {
                e.printStackTrace();
                Timber.e("Unable to access input or output stream from socket");
            }
            inputStream = inputStreamTmp;
            outputStream = outputStreamTmp;
            this.isServer = isServer;
        }

        @Override
        public void run() {
            super.run();
            byte[] buffer = new byte[1024];
            int bytes;
            while (read) {
                try {
                    bytes = inputStream.read(buffer);
                    String value = new String(buffer, 0, bytes);
                    BluetoothClientServer.this.onMessageReceived(value, isServer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                read = false;
                outputStream.close();
                inputStream.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public interface OnClientServerListener {
        void onError(int errorCode);

        void pauseDiscovery(boolean pause);

        void onConnectionAccept(BluetoothSocket bluetoothSocket);

        void onConnectionReject();

        void showDialog(String peerName, OnRequestListener requestListener);

        interface OnRequestListener {
            void onRequestAccepted(boolean accept);
        }
    }
}
