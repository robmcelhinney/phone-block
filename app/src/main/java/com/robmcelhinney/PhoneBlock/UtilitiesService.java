package com.robmcelhinney.PhoneBlock;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

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

    public static boolean isUserNotDriving() { return userNotDriving; }

    public static void setUserNotDriving(boolean newUserNotDriving) {
        userNotDriving = newUserNotDriving;
    }

    private static boolean userNotDriving = false;
}
