package com.wordpress.laaptu.bluetooth.test.refactor.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wordpress.laaptu.bluetooth.R;
import com.wordpress.laaptu.bluetooth.test.refactor.IntentUtils;
import com.wordpress.laaptu.bluetooth.test.refactor.base.ConnectionMonitor;
import com.wordpress.laaptu.bluetooth.test.refactor.base.DataConduit;
import com.wordpress.laaptu.bluetooth.test.refactor.bluetooth.BluetoothConnectionMonitor;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import timber.log.Timber;


public class ChatActivity extends AppCompatActivity implements BluetoothConnectionMonitor.OnConnectionListener {


    private EditText txtSend;
    private Button btnSend;
    private TextView txtChat;

    private DataConduit conduit;
    private ConnectionMonitor connectionMonitor;

    String[] messages = {
            "Hi", "Hello how are you", "I am doing fine out here", "Great", "Good to know you",
            "Bye", "See you tomorrow"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        txtChat = (TextView) findViewById(R.id.chat_txt);
        txtChat.setMovementMethod(new ScrollingMovementMethod());
        txtSend = (EditText) findViewById(R.id.send_txt);
        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChatText();
            }
        });

        if (getIntent() == null || !getIntent().hasExtra(IntentUtils.Extras.ISHOST)) {
            this.finish();
            return;
        }


    }

    int index = 0;

    private Runnable repeatedRunnable = new Runnable() {
        @Override
        public void run() {
            sendRandomText();
        }
    };
    private Handler handler = new Handler();

    private void sendRandomText() {
        handler.postDelayed(repeatedRunnable, 1800);
        index = index > messages.length - 1 ? 0 : index;
        //appendText("Me: ".concat(messages[index]));
        outgoingMessages.add(messages[index]);
        ++index;
    }

    LinkedBlockingQueue<String> outgoingMessages;


    private void sendChatText() {
        String chatText = txtSend.getText().toString();
        if (!TextUtils.isEmpty(chatText)) {
            appendText("Me: ".concat(chatText));
            txtSend.setText("");
            outgoingMessages.add(chatText);
        }
    }

    private void appendText(String text) {
        txtChat.append(text.concat("\n"));
    }

    @Override
    public void connectionLost() {
        this.finish();
    }

    public static class StartComm extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            DataConduit conduit = (DataConduit) params[0];
            conduit.start();
            WeakReference<ChatActivity> activity = (WeakReference<ChatActivity>) params[1];
            if (activity.get() != null) {
                activity.get().startCommunicating();
            }
            return null;
        }
    }

    private Thread dispatchThread;
    private Thread listenThread;
    private boolean keepCommunicating;
    final static int GOOD_CODE = 0xBADFACED;
    protected static final int TYPE_MESSAGE = 0;
    protected static final int TYPE_DISCONNECT = 1;
    protected static final String TAG = "ChatActivity";


    protected void addMessage(String message) {
        if (!TextUtils.isEmpty(message)) {
            txtChat.append("friend: ".concat(message).concat("\n"));
        }
    }

    protected void startCommunicating() {
        keepCommunicating = true;
        listenThread = new Thread("listener") {

            @Override
            public void run() {
                final WeakReference<ChatActivity> activity = new WeakReference<ChatActivity>(ChatActivity.this);
                DataConduit conduit = activity.get().conduit;
                byte message[] = new byte[1024];
                ByteBuffer buffer = ByteBuffer.wrap(message);
                int code;
                int type;
                int size;
                class DispatchMessage implements Runnable {
                    public String text;

                    @Override
                    public void run() {
                        if (activity.get() != null) {
                            activity.get().addMessage(text);
                        }
                    }
                }

                DispatchMessage dispatchToUI = new DispatchMessage();
                while (activity.get() != null && activity.get().keepCommunicating) {
                    buffer.clear();
                    if (conduit != null && !conduit.read(buffer)) {
                        Log.i(TAG, "Quitting read thread");
                        return;
                    }
                    //Log.i(TAG, "" + buffer.limit() + " bytes read");
                    code = buffer.getInt();

                    //Log.i(TAG, "received data");
                    if (code == GOOD_CODE) {
                        type = buffer.getInt();
                        if (type == TYPE_MESSAGE) {
                            size = buffer.getInt();
                            //Log.i(TAG, "" + size + " message bytes read");
                            dispatchToUI.text = new String(message, buffer.position(), size);
                            //Log.i(TAG, "read message " + dispatchToUI.text);
                            activity.get().runOnUiThread(dispatchToUI);
                        } else if (type == TYPE_DISCONNECT) {

                        }
                    }
                }
            }

        };
        listenThread.start();
        dispatchThread = new Thread("dispatcher") {

            @Override
            public void run() {
                try {
                    WeakReference<ChatActivity> activity = new WeakReference<ChatActivity>(ChatActivity.this);
                    DataConduit conduit = activity.get().conduit;
                    byte[] outgoingData = new byte[1024];
                    ByteBuffer outgoingBuffer = ByteBuffer.wrap(outgoingData);
                    while (activity.get() != null && activity.get().keepCommunicating) {
                        String outgoing = activity.get().outgoingMessages.take();
                        outgoingBuffer.clear();
                        outgoingBuffer.putInt(GOOD_CODE);
                        outgoingBuffer.putInt(TYPE_MESSAGE);
                        outgoingBuffer.putInt(outgoing.length());
                        outgoingBuffer.put(outgoing.getBytes(), 0, outgoing.length());

                        //Log.i(TAG, "writing " + outgoingBuffer.position() + " bytes");
                        if (conduit != null && !conduit.write(outgoingBuffer)) {
                            Log.i(TAG, "Quitting write thread");
                            return;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        };
        dispatchThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectionMonitor = new BluetoothConnectionMonitor(this,this,
                BluetoothConnectionMonitor.LISTEN_FOR_BLUETOOTH_CONNECTION);
        connectionMonitor.start();
        String address = getIntent().getStringExtra(IntentUtils.Extras.ADDRESS);
        boolean isHost = getIntent().getBooleanExtra(IntentUtils.Extras.ISHOST,false);
        Timber.d("Address = %s and isHost = %s",address,isHost);


        outgoingMessages = new LinkedBlockingQueue<String>();
        conduit = new DataConduit.TCPBluetooth(address,isHost);
        new StartComm().execute(conduit, new WeakReference<ChatActivity>(this));
        sendRandomText();
    }

    @Override
    protected void onStop() {
        if (conduit != null) {
            conduit.stop();
            conduit = null;
        }
        keepCommunicating = false;
        outgoingMessages.offer("");
        if (listenThread != null) {
            try {
                listenThread.join(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            listenThread = null;
        }
        if (dispatchThread != null) {
            try {
                dispatchThread.join(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            dispatchThread = null;
        }

        handler.removeCallbacks(repeatedRunnable);
        if (connectionMonitor != null) {
            connectionMonitor.stop();
            connectionMonitor = null;
        }
        super.onStop();
    }


}
