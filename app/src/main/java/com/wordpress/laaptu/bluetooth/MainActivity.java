package com.wordpress.laaptu.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.logging.Handler;
import timber.log.Timber;

import static android.R.attr.data;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initPermissions();
  }

  String[] permissions = { android.Manifest.permission.ACCESS_COARSE_LOCATION };
  ArrayList<String> permissionNotGranted = new ArrayList<>();
  static final int RETURN_FROM_REQUEST = 0x10;

  private void initPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      ArrayList<String> permissionList = new ArrayList<>();
      for (String permission : permissions) {
        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED) {
          permissionList.add(permission);
        }
      }
      if (permissionList.size() > 0) {
        String[] permissions = new String[permissionList.size()];
        permissionList.toArray(permissions);
        ActivityCompat.requestPermissions(this, permissions, RETURN_FROM_REQUEST);
        return;
      }
    }
    init();
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case RETURN_FROM_REQUEST:
        for (int i = 0, length = grantResults.length; i < length; ++i) {
          if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
            permissionNotGranted.add(permissions[i]);
          }
        }
        init();
        break;
      default:
        break;
    }
  }

  private void init() {
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

  Set<BluetoothDevice> connectedDevices = new HashSet<>();
  Set<BluetoothDevice> discoveredDevices = new HashSet<>();
  Set<BluetoothDevice> nextDiscoveredDevices = new HashSet<>();
  boolean firstTime = true;
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
      if (intent == null || intent.getAction() == null) return;
      if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
        Timber.d("ACTION_FOUND");
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Timber.d("Discovered device = %s and its address = %s", device.getName(),
            device.getAddress());
        discoveredDevices.add(device);
        if (firstTime) {
          devices = new BluetoothDevice[discoveredDevices.size()];
          discoveredDevices.toArray(devices);
          if (adapter != null) {
            adapter.notifyDataSetChanged();
          }
        }
      } else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
        Timber.d("ACTION_DISCOVERY_STARTED");
        discoveredDevices.clear();
      } else if (intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
        Timber.d("ACTIon_DISCOVERY_FINISHED");
        /**
         * In Nexus 5, Android L, calling
         * startDiscovery() moves the activity
         * to onPause() : Interesting
         * This is how I am planning to do
         * There will be two set<BluetoothDevice>
         *   A and B, and
         *   A -> 1,2,3 which will be shown for first time
         *   and later on
         *   B -> 2,3
         *   So we will find differnece A-B we will get 1 and this
         *   will be our offline device
         *   So total device will be 1,2,3,4 where
         *   2,3,4 are online and 1 will be offline*/
        if (!firstTime) {
          devices = new BluetoothDevice[discoveredDevices.size()];
          discoveredDevices.toArray(devices);
          if (adapter != null) {
            adapter.notifyDataSetChanged();
          }
        }
        firstTime = false;
        startDiscovery();
      }
    }
  };
  IntentFilter deviceDiscoveryIntentFilter = new IntentFilter();
  String[] discoveryIntents = {
      BluetoothDevice.ACTION_FOUND, BluetoothAdapter.ACTION_DISCOVERY_STARTED,
      BluetoothAdapter.ACTION_DISCOVERY_FINISHED
  };

  private void startDiscovery() {
    Timber.d("StartDiscovery");
    /**
     * Seems like startDiscovery is too much resource consuming*/
    if (bluetoothAdapter.isDiscovering()) bluetoothAdapter.cancelDiscovery();
    bluetoothAdapter.startDiscovery();
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

  /**
   * Device finding and
   * getting state of the bluetooth
   */

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
            /**
             * Here we need to close any ongoing task,notify user to again on the bluetooth
             * or else automatically on the bluetooth, depending upon the need*/
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
  private String[] bluetoothActions = {
      BluetoothAdapter.ACTION_STATE_CHANGED,
  };

  private void listenForBluetoothStateChanges() {
    for (String action : bluetoothActions)
      intentFilter.addAction(action);
    //need to add a boolean value to indicate
    for (String action : discoveryIntents) {
      deviceDiscoveryIntentFilter.addAction(action);
    }
    // whether it is registered or not
    registerReceiver(bluetoothBR, intentFilter);
    //for discovering devices
    registerReceiver(deviceDiscoveryBR, deviceDiscoveryIntentFilter);

    getPairedDevices();
    /**
     * The above will only work when
     *  we call start discovery else it won't work
     */
    startDiscovery();
  }

  @Override protected void onPause() {
    super.onPause();
    Timber.d("onPause()");
    try {
      unregisterReceiver(bluetoothBR);
      unregisterReceiver(deviceDiscoveryBR);
      if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
        bluetoothAdapter.cancelDiscovery();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override protected void onResume() {
    super.onResume();
    /**
     * Need to find what actions to be done on Resume()
     * as our broadcast recievers is unregistered*/
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
      View view = getLayoutInflater().inflate(R.layout.list_item, parent, false);
      return new ViewHolderMain(view);
    }

    @Override public void onBindViewHolder(ViewHolderMain holder, int position) {
      final BluetoothDevice device = devices[position];
      String name = "";
      if (device != null) {
        name = device.getName() == null ? name : device.getName();
        holder.textView.setText(name.concat("::").concat(device.getAddress()));
      }
      holder.textView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          onClickItem.onClicked(device);
        }
      });
    }

    @Override public int getItemCount() {
      return devices.length;
    }
  }

  static class ViewHolderMain extends RecyclerView.ViewHolder {
    TextView textView;

    public ViewHolderMain(View itemView) {
      super(itemView);
      textView = (TextView) itemView.findViewById(R.id.txt_name);
    }
  }

  public interface OnClickItem<T> {
    void onClicked(T object);
  }

  private OnClickItem<BluetoothDevice> onClickItem = new OnClickItem<BluetoothDevice>() {
    @Override public void onClicked(BluetoothDevice bluetoothDevice) {
      Toast.makeText(MainActivity.this, bluetoothDevice.getAddress(), Toast.LENGTH_SHORT).show();
    }
  };
}

