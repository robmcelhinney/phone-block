package com.example.rob.FYPDrivingApp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Rob on 25/01/2018.
 */

public class UtilitiesService extends Service {

    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
//        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static boolean isActive() {
        return active;
    }

    public static void setActive(boolean newActive) {
        active = newActive;
    }

    private static boolean active = false;

    public static int getNotifyId() {
        return notifyId;
    }

    private static int notifyId = 0;

    public static boolean isUserNotDriving() { return userNotDriving; }

    public static void setUserNotDriving(boolean newUserNotDriving) {
        userNotDriving = newUserNotDriving;
    }

    private static boolean userNotDriving = false;
}
