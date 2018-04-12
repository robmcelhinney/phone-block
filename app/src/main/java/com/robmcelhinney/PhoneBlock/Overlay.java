package com.robmcelhinney.PhoneBlock;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.rvalerio.fgchecker.AppChecker;

import java.util.HashMap;
import java.util.HashSet;

import static com.robmcelhinney.PhoneBlock.MainActivity.MY_PREFS_NAME;

/**
 * Created by Rob on 08/02/2018.
 */

public class Overlay extends Service {

    private final int delayMillis = 2000;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private AppChecker appChecker;
    private SharedPreferences settings;
    private HashMap closedApps;

    @Override
    public void onCreate() {
        super.onCreate();
        settings = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        appChecker = new AppChecker();
        closedApps = new HashMap<>();

//        handlerLoop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("onstartOverlay", "handler back running");
        handlerLoop();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
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
            if(closedApps.containsKey(fgApp)){
                if((int)closedApps.get(fgApp) == 0) {
                    displayToast("Close app while driving");
                    closedApps.put(fgApp, (int)closedApps.get(fgApp)+1);
                }
                return;
            }
            goHome();
            displayToast("App not allowed while driving");
            closedApps.put(fgApp, 0);
        }
    }

    private void goHome() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }

    private String getForegroundApp(){
        Log.d("CurrApp",  "App is... " + appChecker.getForegroundApp(getApplicationContext()));
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
