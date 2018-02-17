package com.robmcelhinney.FYPDrivingApp;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PermissionsSplashActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 123;

    private boolean isPaused;

    private TextView permissionNeededTextView;

    private TextView skipTextView;

    private Button permissionsButton;

    private static NotificationManager mNotificationManager;

    private final int MY_PERMISSIONS_REQUEST_NOTIFICATION_POLICY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_splash);

        permissionNeededTextView = findViewById(R.id.permissionNeededTextView);

        skipTextView = findViewById(R.id.skipTextView);

        skipTextView.setVisibility(View.INVISIBLE);

        permissionsButton = findViewById(R.id.permissionsButton);
        permissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission(PermissionsSplashActivity.this);
            }
        });

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        isPaused = false;
    }

    public static void checkPermission(Context context) {
        // checks if user gave permission to change notification policy. If not, then launch
        // settings to get them to give permission.
        if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
            context.startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
        }
    }

    // Should return here from settings after granting permission.
    protected void onResume(){
        super.onResume();
        if (isPaused) {
            isPaused = false;
//            if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
//                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
//                skipTextView.setEnabled(true);
//                skipTextView.setVisibility(View.VISIBLE);
//            }
            if(mNotificationManager.isNotificationPolicyAccessGranted()) {
//                Toast.makeText(this, "Congrats! Permission Granted.", Toast.LENGTH_SHORT).show();
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