package com.wordpress.laaptu.bluetooth.test.refactor.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.wordpress.laaptu.bluetooth.R;
import com.wordpress.laaptu.bluetooth.test.refactor.base.ConnectionMonitor;
import com.wordpress.laaptu.bluetooth.test.refactor.bluetooth.BluetoothConnectionMonitor;

public class ConnectActivity extends AppCompatActivity implements ConnectionMonitor.OnConnectionListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        removeThis();
    }


    private ConnectionMonitor connectionMonitor;

    @Override
    protected void onPause() {
        super.onPause();
        if (connectionMonitor != null) {
            connectionMonitor.stop();
            connectionMonitor = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectionMonitor = new BluetoothConnectionMonitor(this, this, BluetoothConnectionMonitor.LISTEN_FOR_BLUETOOTH_DEVICE);
        connectionMonitor.start();
    }

    @Override
    public void connectionLost() {
        finish();
    }

    //TODO remove this onCreate
    private void removeThis() {
        setContentView(R.layout.activity_connect2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }


}
