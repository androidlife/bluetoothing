package com.wordpress.laaptu.bluetooth.test;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.wordpress.laaptu.bluetooth.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Base activity for asking permissions
 * Should be implemented by activities that needs to ask for permissions to the user
 * All permissions needed in the app is asked with this activity
 */
public class PermissionsActivity extends Activity {
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 1001;
    // Required permissions' status
    private static final String[] permissionsRequired = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA, Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };


    @Override
    protected void onResume() {
        super.onResume();
        askForAppPermissions();
    }

    /**
     * Enabling the network device, that is
     * required for any communication
     */
    private void enableNetworkDevice() {

    }

    /**
     * Runtime Permission check for Android 6 and above
     * .........Start
     */

    /**
     * Asks user for permission required for the app
     * If user denies permission it does nothing
     */
    private void askForAppPermissions() {
        final List<String> permissionsList = areAllPermissionsGranted(permissionsRequired);
        // if permissionList is not empty, ask for permissions
        if (permissionsList.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } else {
            enableNetworkDevice();
        }
    }

    /**
     * Checks if the list of permissions are granted
     *
     * @param permissionList list of permissions
     * @return boolean flag, returns false if any one of the given permissions is not granted
     */
    private List<String> areAllPermissionsGranted(String[] permissionList) {
        final List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissionList) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                if (!didUserGrantAllPermissions(grantResults))
                    Toast.makeText(this, R.string.error_few_permission_granted, Toast.LENGTH_SHORT).show();
                else
                    enableNetworkDevice();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Checks if the array of grant results contains negative value
     *
     * @param grantResults array of grant results from onRequestPermissionsRequest
     * @return false if one of the grantResults value is -1, true otherwise
     */
    public boolean didUserGrantAllPermissions(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED)
                return false;

        }
        return true;
    }
    /**
     * Runtime Permission check for Android 6 and above
     * .........End
     */
}
