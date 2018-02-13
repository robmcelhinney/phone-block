package com.robmcelhinney.FYPDrivingApp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.rvalerio.fgchecker.AppChecker;

import java.util.HashSet;

import static com.robmcelhinney.FYPDrivingApp.MainActivity.MY_PREFS_NAME;

/**
 * Created by Rob on 08/02/2018.
 */

public class Overlay extends Service {

    private final int delayMillis = 1000;

    private final Handler handler = new Handler();

    private Runnable runnable;

    private AppChecker appChecker = new AppChecker();

    private SharedPreferences settings;

    @Override
    public void onCreate() {
        super.onCreate();
        settings = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        handlerLoop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handlerLoop() {
        runnable= new Runnable() {
            @Override
            public void run() {
                closeApps();
                handler.postDelayed(this, delayMillis);
            }
        };

        handler.postDelayed(runnable, 3000);
    }

    // TODO Make sure FG app isn't this application.
    private void closeApps() {
        String fgApp = getForegroundApp();
        if (fgApp != null && settings.getStringSet("selectedAppsPackage", new HashSet<String>()).contains(fgApp)) {
            goHome();
            displayToast("App not allowed while Driving");
        }
    }

    private void goHome() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }

    private String getForegroundApp(){
//        Log.d("CurrApp",  "App is... " + appChecker.getForegroundApp(getApplicationContext()));
        return appChecker.getForegroundApp(getApplicationContext());
    }

    private void displayToast(final String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
