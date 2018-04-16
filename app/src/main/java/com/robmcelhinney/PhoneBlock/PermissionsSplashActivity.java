package com.robmcelhinney.PhoneBlock;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class PermissionsSplashActivity extends AppCompatActivity {

    private boolean isPaused;

    private static NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_permissions_splash);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Button permissionsButton = findViewById(R.id.permissionsButton);
        permissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.checkPermission(PermissionsSplashActivity.this);
            }
        });

        isPaused = false;
    }

    // Should return here from settings after granting permission.
    protected void onResume(){
        super.onResume();
        if (isPaused) {
            isPaused = false;
            if(mNotificationManager.isNotificationPolicyAccessGranted()) {
                returnToMain();
            }

        }
    }

    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    private void returnToMain() {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
        edit.putBoolean("pref_previously_started", Boolean.TRUE);
        edit.apply();
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }
}