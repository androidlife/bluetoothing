package com.wordpress.laaptu.bluetooth.test.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

import timber.log.Timber;

import static android.R.attr.port;

public class BluetoothClientServer {
    static final String SERVER_NAME = "LiveTouchChatServer";
    static final UUID SERVER_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a67");

    public static class Error {
        static final int ERROR_SERVER_CREATION = 0x1, ERROR_CONNECT_SERVER_SOCKET = 0x2;
    }

    public interface ErrorEncountered {
        void onError(int errorCode);
    }

    private CreateServerAndListenForClientSocketThread serverThread;
    private ConnectToServerThread connectToServerThread;
    private OnClientServerListener clientServerListener;

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
    }

    private void sendError(int errorCode) {
        if (clientServerListener != null)
            clientServerListener.onError(errorCode);
    }

    private void deliverTheSocket(BluetoothSocket bluetoothSocket) {
        Timber.d("Successfully connected");
        if (clientServerListener != null) {
            clientServerListener.onConnectionAccept(bluetoothSocket);
        }else{
            Timber.d("clientServerListener is null");
        }
    }

    private void pauseDiscovery(boolean pause) {
        if (clientServerListener != null)
            clientServerListener.pauseDiscovery(pause);
    }


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
                }
                if (socket != null) {
                    //connectedIncomingSocketToOurServer(socket);
                    //TODO create a dialog or something for us to accept the communication
                    // if denied, do nothing
                    // else pass the socket and close this
                    Timber.d("This is server now");
                    BluetoothClientServer.this.deliverTheSocket(socket);
                    cancel();
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
                return;
            }
            //connectedToServerOurSocket(socketTobePassedToServer);
            Timber.d("This is client now");
            BluetoothClientServer.this.deliverTheSocket(socketTobePassedToServer);

        }

        public void cancel() {
//            try {
//                socketTobePassedToServer.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    public interface OnClientServerListener {
        void onError(int errorCode);

        void pauseDiscovery(boolean pause);

        void onConnectionAccept(BluetoothSocket bluetoothSocket);

        void onConnectionReject();
    }
}
