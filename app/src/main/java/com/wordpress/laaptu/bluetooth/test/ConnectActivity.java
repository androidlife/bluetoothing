package com.wordpress.laaptu.bluetooth.test;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.wordpress.laaptu.bluetooth.R;

import timber.log.Timber;

import static android.R.style.Theme_Holo_Light_Panel;

public class ConnectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        addDialogFragment();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.d("I am paused");
    }

    private void addDialogFragment() {
        DialogFragment dialogFragment = new SomeDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "SomeDialog");
    }

    public static class SomeDialogFragment extends DialogFragment {
        public SomeDialogFragment() {
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), Theme_Holo_Light_Panel);
            builder.setTitle("Hello there");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return builder.create();
        }
    }

}
