package com.robmcelhinney.PhoneBlock;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import static com.robmcelhinney.PhoneBlock.MainActivity.CHANNEL_ID;

public class DisturbService extends Service{

    private static NotificationManager mNotificationManager;
    private static int prevNotificationFilter = -1;

    private static Context appContext;
    private static final int drivNotiId = 0;

    private static SharedPreferences settings;

    public void onCreate() {
        super.onCreate();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        appContext = getApplicationContext();

        settings = getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            if(intent.hasExtra("disable")) {
                /*Create a boolean that will stop thinking you're driving. What if the passenger is
                connected to the car's bluetooth? They click 'not driving' and it's automatically set again.*/
                userSelectedDoDisturb();
            }
        }
        return START_STICKY;
    }

    public static void userSelectedDoDisturb() {
        UtilitiesService.setUserNotDriving(true);
        if(UtilitiesService.isActive()) {
            doDisturb();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("WrongConstant")
    public static void doNotDisturb() {
        if (mNotificationManager.isNotificationPolicyAccessGranted()) {
            if(mNotificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
                prevNotificationFilter = mNotificationManager.getCurrentInterruptionFilter();

                mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
            }
        }
        else {
            MainActivity.checkPermission(appContext);
        }

        drivingNotification();
        UtilitiesService.setActive(true);
        sendToMainActivity(true);

        UtilitiesService.setUserNotDriving(false);

        if(settings.getBoolean("switchOtherApps", false)) {
            startOverlayService();
        }
    }

    @SuppressLint("WrongConstant")
    public static void doDisturb() {
        if (mNotificationManager.isNotificationPolicyAccessGranted()) {
//            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);

            cancelNotification();

            // sets interruption filter to what it used to be rather than always turning it off.
            if(mNotificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
                if(prevNotificationFilter != -1){
                    mNotificationManager.setInterruptionFilter(prevNotificationFilter);
                }
                else {
                    mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                }
            }
        }

        sendToMainActivity(false);
        DetectDrivingService.setSittingIntoCar(false);
        UtilitiesService.setActive(false);
        stopOverlayService();
    }

    private static void startOverlayService() {
        Intent intent = new Intent(appContext, Overlay.class);
        appContext.startService(intent);
    }

    private static void stopOverlayService() {
        Intent intent = new Intent(appContext, Overlay.class);
        appContext.stopService(intent);
    }

    private static void drivingNotification() {
        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(NOTIFICATION_SERVICE);
        assert notificationManager != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            notificationChannel.setDescription(appContext.getString(R.string.phoneblock_activated));
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent notiIntent = new Intent(appContext, DisturbService.class);
        notiIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notiIntent.putExtra("disable", true);
        notiIntent.putExtra("notification_id", drivNotiId);
        PendingIntent disableIntent = PendingIntent.getService( appContext, 0, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT );


        PendingIntent disableIntent2 = PendingIntent.getActivity( appContext, 1, new Intent(appContext, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon( R.drawable.ic_notify_driving_white )
                .setContentTitle(appContext.getString(R.string.phoneblock_activated))
                .setOngoing(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, appContext.getString(R.string.not_driving), disableIntent)
                .setContentIntent(disableIntent2);

        notificationManager.notify(drivNotiId, builder.build());
    }

    static void cancelNotification() {
        NotificationManager notiMgr = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notiMgr != null;
        notiMgr.cancel(drivNotiId);
    }

    // Necessary to change button check when enabled/disabled.
    private static void sendToMainActivity(boolean value) {
        Intent intent = new Intent("intentToggleButton");
        intent.putExtra("valueBool", value);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);
        SharedPreferences.Editor editPrefs = settings.edit();
        editPrefs.putBoolean("buttonActive", value).apply();
        editPrefs.commit();
    }
}