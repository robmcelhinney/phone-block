package com.example.rob.FYPDrivingApp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.util.Locale;
import java.util.Random;

/**
 * Created by Rob on 27/01/2018.
 */

public class DisturbService extends Service implements TextToSpeech.OnInitListener{

    private NotificationManager mNotificationManager;
    private int prevNotificationFilter;

    private TextToSpeech textToSpeech;

    public void onCreate() {
        super.onCreate();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }

        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setLanguage(Locale.US);
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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void doNotDisturb() {
        if (mNotificationManager.isNotificationPolicyAccessGranted() && mNotificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
            prevNotificationFilter = mNotificationManager.getCurrentInterruptionFilter();

            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
            textToSpeech.speak("Turning on Do not Disturb.", TextToSpeech.QUEUE_ADD, null, Integer.toString(new Random().nextInt()));

            drivingNotification();

            MyUtilities.setActive(true);
        }
    }

    public void doDisturb() {
        if (mNotificationManager.isNotificationPolicyAccessGranted() && mNotificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
//            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);

            // sets interruption filter to what it used to be rather than always turning it off.
            mNotificationManager.setInterruptionFilter(prevNotificationFilter);
            textToSpeech.speak("Turning off Do not Disturb.", TextToSpeech.QUEUE_ADD, null, Integer.toString(new Random().nextInt()));

            MyUtilities.setActive(false);
        }
    }

    @Override
    public void onInit(int status) {

    }


    private void drivingNotification() {
        int notificationId = 0;
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
        notification.setContentText("Do not Disturb Enabled as Driving.");
        notification.setSmallIcon( R.mipmap.ic_launcher );
        notification.setContentTitle( getString( R.string.app_name ) );

        notification.setVisibility(Notification.VISIBILITY_PUBLIC);

        notification.setColor(ContextCompat.getColor(getApplicationContext(), R.color.cast_expanded_controller_ad_container_white_stripe_color));

        Intent intent = new Intent( this, MainActivity.class );
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("disable", true);
        intent.putExtra("notification_id", notificationId);
        PendingIntent disableIntent = PendingIntent.getActivity( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        notification.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Not driving", disableIntent);

        NotificationManager notificationService = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);
        notificationService.notify(0, notification.build());
    }
}


























//package com.example.rob.FYPDrivingApp;
//
//        import android.app.Notification;
//        import android.app.NotificationManager;
//        import android.app.PendingIntent;
//        import android.app.Service;
//        import android.content.BroadcastReceiver;
//        import android.content.Context;
//        import android.content.Intent;
//        import android.content.IntentFilter;
//        import android.os.IBinder;
//        import android.provider.Settings;
//        import android.speech.tts.TextToSpeech;
//        import android.support.annotation.Nullable;
//        import android.support.v4.app.NotificationCompat;
//        import android.support.v4.content.ContextCompat;
//
//        import java.util.Locale;
//        import java.util.Random;
//
///**
// * Created by Rob on 27/01/2018.
// */
//
//public class DisturbService extends Service implements TextToSpeech.OnInitListener{
//
//    private static NotificationManager mNotificationManager;
//    private static int prevNotificationFilter;
//
//    private static TextToSpeech textToSpeech;
//
//    private static Context appContext;
//
//    private static String appName;
//
//    private static Intent notifIntent;
//
//    public void onCreate() {
//        super.onCreate();
//
//        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
//            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
//            startActivity(intent);
//        }
//
//        textToSpeech = new TextToSpeech(this, this);
//        textToSpeech.setLanguage(Locale.US);
//
//
//        appContext = getApplicationContext();
//        appName = getString(R.string.app_name);
//
//        notifIntent = new Intent(this, MainActivity.class);
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return START_STICKY;
////        return super.onStartCommand(intent, flags, startId);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    public static void doNotDisturb() {
//        if (mNotificationManager.isNotificationPolicyAccessGranted() && mNotificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
//            prevNotificationFilter = mNotificationManager.getCurrentInterruptionFilter();
//
//            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
//            textToSpeech.speak("Turning on Do not Disturb.", TextToSpeech.QUEUE_ADD, null, Integer.toString(new Random().nextInt()));
//
//            drivingNotification();
//
//            MyUtilities.setActive(true);
//        }
//    }
//
//    public static void doDisturb() {
//        if (mNotificationManager.isNotificationPolicyAccessGranted() && mNotificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
////            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
//
//            // sets interruption filter to what it used to be rather than always turning it off.
//            mNotificationManager.setInterruptionFilter(prevNotificationFilter);
//            textToSpeech.speak("Turning off Do not Disturb.", TextToSpeech.QUEUE_ADD, null, Integer.toString(new Random().nextInt()));
//
//            MyUtilities.setActive(false);
//        }
//    }
//
//    @Override
//    public void onInit(int status) {
//
//    }
//
//
//    private static void drivingNotification() {
//        int notificationId = 0;
//        NotificationCompat.Builder notification = new NotificationCompat.Builder(appContext);
//        notification.setContentText("Do not Disturb Enabled as Driving.");
//        notification.setSmallIcon( R.mipmap.ic_launcher );
//        notification.setContentTitle( appName );
//
//        notification.setVisibility(Notification.VISIBILITY_PUBLIC);
//
//        notification.setColor(ContextCompat.getColor(appContext, R.color.cast_expanded_controller_ad_container_white_stripe_color));
//
//
//        notifIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        notifIntent.putExtra("disable", true);
//        notifIntent.putExtra("notification_id", notificationId);
//        PendingIntent disableIntent = PendingIntent.getActivity( appContext, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT );
//        notification.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Not driving", disableIntent);
//
//        NotificationManager notificationService = (NotificationManager)appContext.getSystemService(NOTIFICATION_SERVICE);
//        notificationService.notify(0, notification.build());
//    }
//}
