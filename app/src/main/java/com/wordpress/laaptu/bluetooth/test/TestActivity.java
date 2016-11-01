package com.wordpress.laaptu.bluetooth.test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wordpress.laaptu.bluetooth.R;
import com.wordpress.laaptu.bluetooth.test.bluetooth.StoredBT;
import com.wordpress.laaptu.bluetooth.test.refactor.IntentUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import timber.log.Timber;

import static android.R.string.cancel;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        addFirstFragment();
        //bluetoothTest();
        //createBluetooth();
        threadTest();

    }

    private Handler handler;

    private void threadTest() {
        textView = (TextView) findViewById(R.id.info_txt);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 10) {
                    printValue((Integer) msg.obj);
                }
            }
        };
    }

    private class SomeThread extends HandlerThread {
        private boolean start = true;
        int a = 0;
        private Handler handler;

        public SomeThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();

        }


        BluetoothServerSocket serverSocket = null;
        public void runNow() {

            try {
                serverSocket = BluetoothAdapter.getDefaultAdapter().
                        listenUsingInsecureRfcommWithServiceRecord(
                        IntentUtils.ServerInfo.SERVER_NAME, IntentUtils.ServerInfo.SERVER_UUID);
            } catch (Exception e) {
                e.printStackTrace();
            }

            BluetoothSocket socket = null;
            while (start) {
                try {
                    Timber.d("Server created succssfully");
                    socket = serverSocket.accept();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Timber.d("Listening for accept() = %d", ++a);

//                for (int i = 0; i < 10; ++i)
//                    ++a;
//                Timber.d("The value of a = %d", a);
//                if (a > 100) {
//                    cancel();
//                    //TestActivity.this.printValue(a);
//                    Message message = Message.obtain();
//                    message.what =10;
//                    message.obj =a;
//                    handler.sendMessage(message);
//                }

            }
        }

        public void cancel() {
            Timber.d("Cancel is called");
            start = false;
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Timber.d("Server socket closed");
        }
        public void cancelNow(){
            Timber.d("Cancel now is called");
            //handler.dis
          handler.sendEmptyMessage(10);
            //handler.sendEmptyMessage(10);
        }
        public void startNow(){
            start();
            handler =new Handler(this.getLooper()){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if(msg.what== 11){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                runNow();
                            }
                        });
                        return;
                    }
                    if(msg.what == 10){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Timber.d("Msg what cancel received");
                                cancel();
                            }
                        });

                    }
                }
            };
            handler.sendEmptyMessage(11);
        }
    }

    private SomeThread someThread;
    private TextView textView;

    public void startThread(View view) {
        someThread = new SomeThread("Hello");
        someThread.startNow();
    }

    public void printValue(int val) {
        textView.setText(String.valueOf(val));
        stopThread(null);
    }

    public void stopThread(View view) {
        if (someThread != null) {
            someThread.cancelNow();
            someThread = null;
        }
    }


    private BluetoothAdapter bluetoothAdapter;

    private void bluetoothTest() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.startDiscovery();
    }

    private static final String BT_NOTE3 = "38:94:96:F2:37:60";

    private void createBluetooth() {
        final BluetoothDevice device = StoredBT.getInstance().getBluetoothDevice();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(device.getAddress(), 1);
                    String address = new String(socket.getInetAddress().getAddress());
                    System.out.println(address);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.d("ON PAUSE");
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {

        }
        if (bluetoothAdapter != null)
            bluetoothAdapter.cancelDiscovery();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Timber.d("Device found =%s", device.getAddress());
            }

        }
    };

    private IntentFilter foundAction;

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("ON RESUME");
        foundAction = new IntentFilter();
        foundAction.addAction(BluetoothDevice.ACTION_FOUND);
        //registerReceiver(receiver, foundAction);
        //bluetoothTest();
    }

    private void addFirstFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new FirstFragment()).commit();

    }

    private void addSecondFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new SecondFragment())
                .addToBackStack(null).commit();

    }

    public static class FirstFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_first, container, false);
        }

        @Override
        public void onPause() {
            super.onPause();
//            Timber.d("FirstFragment Paused");
        }

        @Override
        public void onResume() {
            super.onResume();
//            Timber.d("FirstFragment Resumed");
        }
    }


    public static class SecondFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_second, container, false);
        }

        @Override
        public void onPause() {
            super.onPause();
            Timber.d("SecondFragment Paused");
        }

        @Override
        public void onResume() {
            super.onResume();
            Timber.d("SecondFragment Resumed");
        }
    }
}

