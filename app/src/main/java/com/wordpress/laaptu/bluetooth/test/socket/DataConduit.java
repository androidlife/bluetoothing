package com.wordpress.laaptu.bluetooth.test.socket;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.wordpress.laaptu.bluetooth.test.bluetooth.BluetoothClientServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.UUID;

import timber.log.Timber;

public interface DataConduit {
    int TIMEOUT = 90000;

    public void start();

    public void stop();

    public boolean read(ByteBuffer incoming);

    public boolean write(ByteBuffer outgoing);

    public static class UDP implements DataConduit {
        private static final String TAG = "UDPDataConduit";
        private byte[] temp;
        private DatagramSocket socket;
        private DatagramPacket incoming;
        private DatagramPacket outgoing;
        private InetAddress clientAddress;
        private String clientIp;
        private int localPort;
        private int remotePort;

        public UDP(int localPort, String clientIp, int remotePort) {
            this.localPort = localPort;
            this.clientIp = clientIp;
            this.remotePort = remotePort;
            temp = new byte[1];
        }

        @Override
        public void start() {
            try {
                clientAddress = InetAddress.getByName(clientIp);
                if (clientAddress != null && NetworkInterface.getByInetAddress(clientAddress) != null) {
                    Log.i(TAG, "self referencing, waiting to deduce client");
                    clientAddress = null;
                }
                socket = new DatagramSocket(localPort);
                socket.setSoTimeout(TIMEOUT);
                incoming = new DatagramPacket(temp, 1);
                outgoing = new DatagramPacket(temp, 1, clientAddress, remotePort);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void stop() {
            socket.close();
        }

        @Override
        public boolean read(ByteBuffer incomingData) {
            try {
                incoming.setData(incomingData.array());
                socket.receive(incoming);
                if (clientAddress == null) {
                    clientAddress = incoming.getAddress();
                    Log.i(TAG, "deduced client address " + clientAddress.getHostAddress());
                }
                incomingData.position(incoming.getOffset());
                incomingData.limit(incoming.getLength());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public boolean write(ByteBuffer outgoingData) {
            try {
                if (clientAddress != null) {
                    if (outgoing.getAddress() == null) {
                        outgoing.setAddress(clientAddress);
                    }
                    outgoing.setData(outgoingData.array(), 0, outgoingData.position());
                    socket.send(outgoing);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public static class TCP implements DataConduit {

        private static final String TAG = "TCPDataConduit";
        private Socket socket;
        private int localPort;
        private String clientIp;
        private int remotePort;
        private InputStream inputStream;
        private OutputStream outputStream;
        private ByteBuffer inSize;
        private ByteBuffer outSize;
        private boolean isHost;

        public TCP(int localPort, String clientIp, int remotePort, boolean isHost) {
            this.localPort = localPort;
            this.remotePort = remotePort;
            this.clientIp = clientIp;
            this.isHost = isHost;
            inSize = ByteBuffer.allocate(4);
            outSize = ByteBuffer.allocate(4);
        }

        @Override
        public void start() {
            int retryCount = 0;
            if (isHost) {
                ServerSocket server = null;
                try {
                    server = new ServerSocket(localPort);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                while (server != null && socket == null && retryCount < 5) {
                    try {
                        socket = server.accept();
                    } catch (IOException e) {
                        ++retryCount;
                        e.printStackTrace();
                    }
                }
                if (server != null) {
                    try {
                        server.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.i(TAG, "Server connection accepted");
            } else {
                while (socket == null && retryCount < 5) {
                    try {
                        socket = new Socket(clientIp, remotePort);
                        Log.i(TAG, "Client connection accepted");
                    } catch (IOException e) {
                        ++retryCount;
                        e.printStackTrace();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
            if (socket != null) {
                try {
                    inputStream = socket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    outputStream = socket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void stop() {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                inputStream = null;
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                outputStream = null;
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = null;
            }
        }

        private boolean actualRead(ByteBuffer incoming) throws IOException {
            int bytesRemaining = incoming.limit();
            int readOffset = 0;
            int bytesRead = 0;
            while (bytesRemaining > 0) {
                bytesRead = inputStream.read(incoming.array(), readOffset, bytesRemaining);
                if (bytesRead < 0) {
                    Log.w(TAG, "read EOF");
                    return false;
                }
                readOffset += bytesRead;
                bytesRemaining -= bytesRead;
            }
            return (bytesRemaining == 0);
        }

        @Override
        public boolean read(ByteBuffer incoming) {
            if (inputStream != null) {
                try {
                    inSize.clear();
                    if (!actualRead(inSize)) {
                        Log.e(TAG, "Error reading incoming packet size");
                        return false;
                    }
                    incoming.limit(inSize.getInt());
                    if (!actualRead(incoming)) {
                        Log.e(TAG, "Error reading incoming packet of " + incoming.limit() + " bytes");
                    }
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.e(TAG, "InputStream invalid");
            return false;
        }

        @Override
        public boolean write(ByteBuffer outgoing) {
            if (outputStream != null) {
                try {
                    outSize.clear();
                    int size = outgoing.position();
                    outSize.putInt(size);
                    outputStream.write(outSize.array(), 0, outSize.limit());
                    outputStream.write(outgoing.array(), 0, size);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.e(TAG, "OutputStream invalid");
            return false;
        }

    }

    public static final String SERVER_NAME = "LiveTouchChatServer";
    public static final UUID SERVER_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a67");

    public static class TCPBluetooth implements DataConduit {

        private static final String TAG = "TCPDataConduit";
        //		private Socket socket;
        private int localPort;
        private String clientIp;
        private int remotePort;
        private InputStream inputStream;
        private OutputStream outputStream;
        private ByteBuffer inSize;
        private ByteBuffer outSize;
        private boolean isHost;
        private BluetoothSocket socket;

        public TCPBluetooth(String clientIp, boolean isHost) {
            this.clientIp = clientIp;
            this.isHost = isHost;
            //socket = SocketProvider.getInstance().socket;
            inSize = ByteBuffer.allocate(4);
            outSize = ByteBuffer.allocate(4);
        }

        private void createServer() {
            int retryCount = 0;
            BluetoothServerSocket server = null;
            try {
                server = BluetoothAdapter.getDefaultAdapter().listenUsingInsecureRfcommWithServiceRecord(SERVER_NAME, SERVER_UUID);
            } catch (Exception e) {
                Timber.d("Unable to create a server ");
                e.printStackTrace();
                //BluetoothClientServer.this.sendError(BluetoothClientServer.Error.ERROR_SERVER_CREATION);
            }

            while (server != null && socket == null && retryCount < 5) {
                try {
                    socket = server.accept();
                } catch (IOException e) {
                    ++retryCount;
                    e.printStackTrace();
                }
            }
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "Server connection accepted");
        }

        private void connectToServer() {
            int retryCount = 0;
            while (socket == null && retryCount < 5) {
                try {
                    socket = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(clientIp).createInsecureRfcommSocketToServiceRecord(SERVER_UUID);
                    socket.connect();
                } catch (Exception e) {
                    ++retryCount;
                    Timber.e(
                            "Cannot create socket that needs to be passed to the server, going for fallback socket");
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void start() {
            if (isHost)
                createServer();
            else
                connectToServer();
            if (socket != null) {
                try {
                    inputStream = socket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    outputStream = socket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void stop() {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                inputStream = null;
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                outputStream = null;
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = null;
            }
        }

        private boolean actualRead(ByteBuffer incoming) throws IOException {
            int bytesRemaining = incoming.limit();
            int readOffset = 0;
            int bytesRead = 0;
            while (bytesRemaining > 0) {
                bytesRead = inputStream.read(incoming.array(), readOffset, bytesRemaining);
                if (bytesRead < 0) {
                    Log.w(TAG, "read EOF");
                    return false;
                }
                readOffset += bytesRead;
                bytesRemaining -= bytesRead;
            }
            return (bytesRemaining == 0);
        }

        @Override
        public boolean read(ByteBuffer incoming) {
            if (inputStream != null) {
                try {
                    inSize.clear();
                    if (!actualRead(inSize)) {
                        Log.e(TAG, "Error reading incoming packet size");
                        return false;
                    }
                    incoming.limit(inSize.getInt());
                    if (!actualRead(incoming)) {
                        Log.e(TAG, "Error reading incoming packet of " + incoming.limit() + " bytes");
                    }
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.e(TAG, "InputStream invalid");
            return false;
        }

        @Override
        public boolean write(ByteBuffer outgoing) {
            if (outputStream != null) {
                try {
                    outSize.clear();
                    int size = outgoing.position();
                    outSize.putInt(size);
                    outputStream.write(outSize.array(), 0, outSize.limit());
                    outputStream.write(outgoing.array(), 0, size);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.e(TAG, "OutputStream invalid");
            return false;
        }

    }
}
