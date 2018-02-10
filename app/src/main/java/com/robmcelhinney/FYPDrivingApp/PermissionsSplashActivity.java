package com.robmcelhinney.FYPDrivingApp;

import android.*;
import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rob.FYPDrivingApp.*;
import com.example.rob.FYPDrivingApp.R;

import org.apache.commons.lang3.ArrayUtils;

public class PermissionsSplashActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 123;

    private boolean isPaused;

    private TextView permissionNeededTextView;

    private TextView skipTextView;

    Button permissionsButton;

    private NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.rob.FYPDrivingApp.R.layout.activity_permissions_splash);

        permissionNeededTextView = (TextView) findViewById(R.id.permissionNeededTextView);

        skipTextView = (TextView) findViewById(R.id.skipTextView);

        skipTextView.setVisibility(View.INVISIBLE);

        permissionsButton = (Button) findViewById(R.id.permissionsButton);
        permissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
            }
        });

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        isPaused = false;
    }

    private void checkPermission() {
        // checks if user gave permission to change notification policy. If not, then launch
        // settings to get them to give permission.
        if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }

//        ActivityCompat.requestPermissions(this,
//                new String[]{Manifest.permission.ACCESS_NOTIFICATION_POLICY},
//                1);
//        if(mNotificationManager.isNotificationPolicyAccessGranted()) {
//            startActivity(new Intent(PermissionsSplashActivity.this, MainActivity.class));
//        }
    }

    // Should return here from settings after granting permission.
    protected void onResume(){
        super.onResume();
        if (isPaused) {
            isPaused = false;
            if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                skipTextView.setEnabled(true);
                skipTextView.setVisibility(View.VISIBLE);
            }
            else {
                Toast.makeText(this, "Congrats! Permission Granted.", Toast.LENGTH_SHORT).show();
                returnToMain();
            }

        }
    }

    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    public void onClick(View v) {
        returnToMain();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantedResults) {
//        switch (requestCode) {
//            case 1: {
//
//                // If request is cancelled, the result arrays are empty.
//                if (ArrayUtils.isEmpty(grantedResults)
//                        && grantedResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "Congrats!", Toast.LENGTH_SHORT).show();
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }

    private void returnToMain() {
        finish();
    }







}

