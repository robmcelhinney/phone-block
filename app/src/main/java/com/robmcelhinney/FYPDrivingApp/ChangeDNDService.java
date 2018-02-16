package com.robmcelhinney.FYPDrivingApp;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Objects;

/**
 * Created by Rob on 25/01/2018.
 */

public class ChangeDNDService extends Service {

    public void onCreate() {
        super.onCreate();

        BroadcastReceiver doNotDisturbBroadcastReceiver = new doNotDisturbBroadcastReceiver();
        IntentFilter intentChangingDND = new IntentFilter();
        intentChangingDND.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
        registerReceiver(doNotDisturbBroadcastReceiver, intentChangingDND);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
//        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        displayNotification("destroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class doNotDisturbBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Objects.equals(intent.getAction(), (NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED))) {

//                displayNotification("isActive? " + UtilitiesService.isActive());

                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                assert mNotificationManager != null;
                if (mNotificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_PRIORITY && UtilitiesService.isActive()) {
                    cancelNotification(getApplicationContext(), UtilitiesService.getNotifyId());
                    UtilitiesService.setActive(false);
                }
            }
        }
    }

    public static void cancelNotification(Context context, int notifyId) {
        NotificationManager notiMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notiMgr != null;
        notiMgr.cancel(notifyId);
    }

    public void displayData() {
        SharedPreferences sharedPref = getSharedPreferences("activeInfo", Context.MODE_PRIVATE);

        Boolean active = sharedPref.getBoolean("activeBool", false);
    }

    private void displayNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentText(message);
        builder.setSmallIcon( R.mipmap.ic_launcher );
        builder.setContentTitle( getString( R.string.app_name ) );
        NotificationManagerCompat.from(this).notify(10, builder.build());
    }
}