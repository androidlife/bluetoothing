package com.wordpress.laaptu.bluetooth.test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wordpress.laaptu.bluetooth.R;

import timber.log.Timber;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        addFirstFragment();
        //bluetoothTest();
    }
    private BluetoothAdapter bluetoothAdapter;
    private void bluetoothTest(){
         bluetoothAdapter =BluetoothAdapter.getDefaultAdapter();
         bluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.d("ON PAUSE");
        try{
            unregisterReceiver(receiver);
        }catch(Exception e){

        }
        if (bluetoothAdapter != null)
            bluetoothAdapter.cancelDiscovery();
    }

    private final BroadcastReceiver receiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Timber.d("Device found =%s",device.getAddress());
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
        registerReceiver(receiver,foundAction);
        bluetoothTest();
    }

    private void addFirstFragment(){
        getSupportFragmentManager().beginTransaction().replace(R.id.container,new FirstFragment()).commit();

    }
    private void addSecondFragment(){
        getSupportFragmentManager().beginTransaction().replace(R.id.container,new SecondFragment())
                .addToBackStack(null).commit();

    }

    public static class FirstFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_first,container,false);
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


    public static class SecondFragment extends Fragment{
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_second,container,false);
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

