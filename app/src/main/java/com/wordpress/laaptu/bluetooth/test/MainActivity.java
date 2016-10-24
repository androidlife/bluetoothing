package com.wordpress.laaptu.bluetooth.test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wordpress.laaptu.bluetooth.R;

public class MainActivity extends PermissionsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }

    @Override
    public void onDeviceActivated() {
        super.onDeviceActivated();
        startActivityForResult(new Intent(this, ConnectActivity.class), RETURN_FROM_CONNECT);
    }
}
