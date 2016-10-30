package com.wordpress.laaptu.bluetooth.test.refactor.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.wordpress.laaptu.bluetooth.R;
import com.wordpress.laaptu.bluetooth.test.refactor.Extras;
import com.wordpress.laaptu.bluetooth.test.refactor.base.ConnectionMonitor;

import timber.log.Timber;

public class ConnectActivity extends FragmentActivity implements ConnectionMonitor.OnConnectionListener {


    private static final String TAG = "ConnectActivity";
    private OnlineFragment fragment;
    //private ConnectionMonitor connectionMonitor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        fragment = new OnlineFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }


    @Override
    protected void onPause() {
        Timber.d("onPause()");
//        if (connectionMonitor != null) {
//            connectionMonitor.stop();
//            connectionMonitor = null;
//        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        Timber.d("onResume()");
        //connectionMonitor = new BluetoothConnectionMonitor(this, this, BluetoothConnectionMonitor.LISTEN_FOR_BLUETOOTH_DEVICE);
        //connectionMonitor.start();
        Intent intent = getIntent();
        setThemeAsPerIntentAction(intent);
        if (fragment != null)
            fragment.passIntent(intent);
        super.onResume();
    }

    private void setThemeAsPerIntentAction(Intent intent) {
        String action = intent.getStringExtra("action");
        if (action == null || action.isEmpty()) {
            Timber.e("Action is invalid");
        }
        int themeId = -1;
        if (Extras.ACTION_TOUCHTRAILS.equals(action)
                || Extras.ACTION_TOUCHDICE.equals(action)) {
            themeId = android.R.style.Theme_Holo_NoActionBar_Fullscreen;
        } else if (Extras.ACTION_TOUCHVIDEO.equals(action) ||
                Extras.ACTION_TOUCHCHAT.equals(action)) {
            themeId = android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen;
        }
        if (themeId != -1) {
            setTheme(themeId);
        }
    }

    @Override
    public void connectionLost() {
        finish();
    }

    //Need to look upon this
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Connection to peer lost", Toast.LENGTH_LONG).show();
        }
    }

/**
 * Code commented right now as it doesn't seem to be used
 */
//
//    public static class LoginFragment extends DialogFragment {
//        private EditText username;
//        private EditText password;
//        private ClickListener loginListener;
//        private ClickListener registrationListener;
//
//        public interface ClickListener {
//            public abstract void OnClick(View v, String username, String password);
//        }
//
//        public void setLoginListener(ClickListener login) {
//            loginListener = login;
//        }
//
//        public void setRegistrationListener(ClickListener registration) {
//            registrationListener = registration;
//        }
//
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Holo_Light_Panel);
//            LayoutInflater inflater = getActivity().getLayoutInflater();
//            View layoutView = inflater.inflate(R.layout.login, null);
//            builder.setView(layoutView);
//            final Dialog dialog = builder.create();
//            username = (EditText) layoutView.findViewById(R.id.userName);
//            password = (EditText) layoutView.findViewById(R.id.password);
//            layoutView.findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String user = username.getText().toString();
//                    String pass = password.getText().toString();
//                    loginListener.OnClick(v, user, pass);
//                    v.setOnClickListener(null);
//                    dialog.dismiss();
//                }
//            });
//            layoutView.findViewById(R.id.registerNewAccount).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String user = username.getText().toString();
//                    String pass = password.getText().toString();
//                    registrationListener.OnClick(v, user, pass);
//                    v.setOnClickListener(null);
//                    dialog.dismiss();
//                }
//            });
//            return dialog;
//        }
//
//        @Override
//        public void onDismiss(DialogInterface dialog) {
//            loginListener = null;
//            registrationListener = null;
//            super.onDismiss(dialog);
//        }
//    }
//
//    ;
//
//    public static class AddContactFragment extends DialogFragment {
//        private EditText username;
//        private EditText fullname;
//        private ClickListener addListener;
//
//        public interface ClickListener {
//            public abstract void OnClick(View v, String username, String name);
//        }
//
//        public void setAddListener(ClickListener login) {
//            addListener = login;
//        }
//
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Holo_Light_Panel);
//            LayoutInflater inflater = getActivity().getLayoutInflater();
//            View layoutView = inflater.inflate(R.layout.add_contact, null);
//            builder.setView(layoutView);
//            Dialog dialog = builder.create();
//            username = (EditText) layoutView.findViewById(R.id.userName);
//            fullname = (EditText) layoutView.findViewById(R.id.fullName);
//            layoutView.findViewById(R.id.addContact).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String user = username.getText().toString();
//                    String name = fullname.getText().toString();
//                    addListener.OnClick(v, user, name);
//                }
//            });
//            return dialog;
//        }
//
//        @Override
//        public void onDismiss(DialogInterface dialog) {
//            getDialog().findViewById(R.id.addContact).setOnClickListener(null);
//            addListener = null;
//            super.onDismiss(dialog);
//        }
//
//    }
//

}
