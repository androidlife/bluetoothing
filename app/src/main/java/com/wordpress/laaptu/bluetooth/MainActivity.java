package com.wordpress.laaptu.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import timber.log.Timber;

import static android.R.attr.data;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initViews();
    bluetoothTest();
  }

  private void initViews() {
    recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    recyclerView.setLayoutManager(linearLayoutManager);
    adapter = new RecyclerAdapter();
    recyclerView.setAdapter(adapter);
  }

  BluetoothAdapter bluetoothAdapter;
  static final int REQUEST_BLUETOOTH_ENABLE = 0x9;

  /**
   * */
  BluetoothServerSocket serverSocket;

  private void createServer() {
    if (true) return;
    try {
      serverSocket =
          bluetoothAdapter.listenUsingRfcommWithServiceRecord("FirstChat", UUID.randomUUID());
      new AcceptSocketThread().start();
    } catch (IOException e) {
      Timber.e("Unable to create server socket");
      e.printStackTrace();
    }
  }

  Set<BluetoothDevice> connectedDevices = new HashSet<>();
  Set<BluetoothDevice> discoveredDevices = new HashSet<>();
  /**
   * This will only work
   * when bluetoothAdapter startDiscovery() is called
   * else it won't work
   * For startDiscovery() to work, you need to provide
   * permission BLUETOOTH_ADMIN
   */
  private final BroadcastReceiver deviceDiscoveryBR = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      Timber.d("Device discovered bluetooth receiver");
      if (intent != null && intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
        Timber.d("ACTION_FOUND");
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Timber.d("Discovered device = %s and its address = %s", device.getName(),
            device.getAddress());
        discoveredDevices.add(device);
        devices = new BluetoothDevice[discoveredDevices.size()];
        discoveredDevices.toArray(devices);
        if (adapter != null) {
          adapter.notifyDataSetChanged();
        }
      } else if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
        Timber.d("ACTION_ACL_CONNECTED");
      } else if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
        Timber.d("ACTION_ACL_DISCONNECTED");
      } else if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
        Timber.d("ACTION_BOND_STATE_CHANGED");
      }
    }
  };
  IntentFilter deviceDiscoveryIntentFilter = new IntentFilter();
  String[] discoveryIntents = {
      BluetoothDevice.ACTION_FOUND, BluetoothDevice.ACTION_ACL_CONNECTED,
      BluetoothDevice.ACTION_ACL_DISCONNECTED, BluetoothDevice.ACTION_BOND_STATE_CHANGED,
      BluetoothDevice.ACTION_CLASS_CHANGED
  };

  private Handler discoveryHandler = new Handler();
  private Runnable discoveryRunnable = new Runnable() {
    @Override public void run() {
      startDiscovery();
    }
  };

  private void startDiscovery() {
    Timber.d("StartDiscovery");
    discoveryHandler.postDelayed(discoveryRunnable, 2000);
    //bluetoothAdapter.startDiscovery();
  }

  /**
   * This function only lists outs the
   * previously paired devices
   * The chances are the paired device may be present now
   * or may not be present
   */
  private void getPairedDevices() {

    connectedDevices.addAll(bluetoothAdapter.getBondedDevices());
    if (connectedDevices.size() > 0) {
      Timber.d("The total number of connected devices = %d", connectedDevices.size());
      for (BluetoothDevice device : connectedDevices)
        Timber.d("Device = %s and its address = %s", device.getName(), device.getAddress());
    } else {
      Timber.d("There are no connected devices");
    }
  }

  private class AcceptSocketThread extends Thread {
    @Override public void run() {
      try {
        BluetoothSocket bluetoothSocket = serverSocket.accept();
      } catch (IOException e) {
        Timber.e("Unable to accept the socket");
        e.printStackTrace();
      }
    }
  }

  /**
   * Device finding and
   * getting state of the bluetooth
   */
  private BluetoothDevice myBluetoothDevice;

  private void bluetoothTest() {
    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (bluetoothAdapter == null) {
      // device doesn't support bluetooth
      return;
    }
    if (!bluetoothAdapter.isEnabled()) {
      //https://developer.android.com/guide/topics/connectivity/bluetooth.html#SettingUp
      Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      //startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE);
      // or you can make discoverable for indefinite time
      //https://developer.android.com/guide/topics/connectivity/bluetooth.html#EnablingDiscoverability
      // in some devices you can make the device discoverable
      // for certain duration only
      // the default value is 120 sec and setting it 0 will make it
      // being discoverable for infinite time
      Intent intent1 = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
      intent1.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
      startActivityForResult(intent1, REQUEST_BLUETOOTH_ENABLE);
    } else {
      Timber.d("Bluetooth is enabled");
      listenForBluetoothStateChanges();
      //lets see what information of our bluetooth is there
      /**
       * Seems like we can't access the bluetoothDevice of our own
       * but we do get the information like address and name*/

      Timber.d("This device address =%s and name =%s",
          bluetoothAdapter.getDefaultAdapter().getAddress(), bluetoothAdapter.getName());
    }
  }

  private final BroadcastReceiver bluetoothBR = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      if (intent != null && intent.hasExtra(BluetoothAdapter.EXTRA_STATE)) {
        int extraState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
        int extraPreviousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);
        Timber.d("extraState = %d , extraPreviousState = %d", extraState, extraPreviousState);
        switch (extraState) {
          case BluetoothAdapter.STATE_TURNING_ON:
            Timber.d("Bluetooth state = state turning on");
            break;
          case BluetoothAdapter.STATE_ON:
            Timber.d("Bluetooth state = state on");
            break;
          case BluetoothAdapter.STATE_TURNING_OFF:
            Timber.d("Bluetooth state = state turning off");
            break;
          case BluetoothAdapter.STATE_OFF:
            Timber.d("Bluetooth state = state off");
            break;
        }
      }
    }
  };
  private final IntentFilter intentFilter = new IntentFilter();
  /**
   * Right now the importance is only on ACTION_STATE_CHANGED as it provides
   * possible values in extra fields like STATE_TURNING_ON,STATE_ON and so on
   * STATE_TURNING_OFF,STATE_OFF
   */
  private String[] actions = {
      BluetoothAdapter.ACTION_STATE_CHANGED, BluetoothAdapter.ACTION_DISCOVERY_STARTED,
      BluetoothAdapter.ACTION_DISCOVERY_FINISHED, BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED
  };

  private void listenForBluetoothStateChanges() {
    for (String action : actions)
      intentFilter.addAction(action);
    //need to add a boolean value to indicate
    for (String action : discoveryIntents)
      deviceDiscoveryIntentFilter.addAction(action);
    // whether it is registered or not
    registerReceiver(bluetoothBR, intentFilter);
    //for discovering devices
    registerReceiver(deviceDiscoveryBR, deviceDiscoveryIntentFilter);
    /**
     * The above will only work when
     *  we call start discovery else it won't work
     */
    startDiscovery();
    getPairedDevices();

    //create a server
    createServer();
  }

  @Override protected void onPause() {
    super.onPause();
    try {
      unregisterReceiver(bluetoothBR);
      unregisterReceiver(deviceDiscoveryBR);
      discoveryHandler.removeCallbacks(discoveryRunnable);
      //serverSocket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_BLUETOOTH_ENABLE) {
      Timber.d("Result code = %d", resultCode);
      /**
       * When just requesting with ACTION_REQUEST_ENABLE
       * resultCode = -1 i.e RESULT _OK if user grants
       * permission
       * and requesting with ACTION_REQUEST_DISCOVERABLE
       * resultCode = 1 i.e. RESULT_FIRST_USER if user
       * grants permission
       * and if user doesn't give permssion
       * resultCode = 0 i.e. RESULT_CANCELLED*/
      if (resultCode == RESULT_OK || resultCode == RESULT_FIRST_USER) {
        Timber.d("Bluetooth access given by user");
        listenForBluetoothStateChanges();
      } else {
        Timber.d("Bluetooth access not given by user");
      }
    }
  }

  RecyclerView recyclerView;
  RecyclerAdapter adapter;
  BluetoothDevice[] devices = new BluetoothDevice[0];

  /**
   * RecyclerView changes
   */

  private class RecyclerAdapter extends RecyclerView.Adapter<ViewHolderMain> {
    @Override public ViewHolderMain onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
      return new ViewHolderMain(view);
    }

    @Override public void onBindViewHolder(ViewHolderMain holder, int position) {
      BluetoothDevice device = devices[position];
      if (device != null) {
        holder.textView.setText(device.getName().concat("::").concat(device.getAddress()));
      }
    }

    @Override public int getItemCount() {
      return devices.length;
    }
  }

  static class ViewHolderMain extends RecyclerView.ViewHolder {
    TextView textView;

    public ViewHolderMain(View itemView) {
      super(itemView);
      textView = (TextView) itemView.findViewById(android.R.id.text1);
    }
  }
}

