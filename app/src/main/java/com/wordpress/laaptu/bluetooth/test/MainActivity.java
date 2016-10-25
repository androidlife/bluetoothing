package com.wordpress.laaptu.bluetooth.test;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.wordpress.laaptu.bluetooth.R;
import com.wordpress.laaptu.bluetooth.test.connect.ConnectActivity;

public class MainActivity extends PermissionsActivity {

    private Button btnGo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        btnGo =(Button)findViewById(R.id.btn_go);
        //btnGo.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //btnGo.setEnabled(false);
    }

    @Override
    public void onDeviceActivated() {
        super.onDeviceActivated();
        //btnGo.setEnabled(true);

    }

    public void goToChat(View view){
        startActivityForResult(new Intent(this, ConnectActivity.class), RETURN_FROM_CONNECT);
    }
}
