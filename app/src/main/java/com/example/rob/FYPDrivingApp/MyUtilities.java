package com.example.rob.FYPDrivingApp;

import android.content.SharedPreferences;
import android.view.View;

/**
 * Created by Rob on 25/01/2018.
 */

public final class MyUtilities {
    public static int getNotifyId() {
        return notifyId;
    }

    public static int notifyId = 0;



    public static boolean isActive() {
        return active;
    }

    public static void setActive(boolean newActive) {
        active = newActive;
    }

    public static boolean active = false;
}
