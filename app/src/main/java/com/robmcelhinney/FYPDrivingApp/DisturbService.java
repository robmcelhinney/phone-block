package com.robmcelhinney.FYPDrivingApp;

        import android.app.Notification;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.app.Service;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.media.Ringtone;
        import android.media.RingtoneManager;
        import android.os.IBinder;
        import android.speech.tts.TextToSpeech;
        import android.support.annotation.Nullable;
        import android.support.v4.app.NotificationCompat;
        import android.support.v4.content.ContextCompat;
        import android.support.v4.content.LocalBroadcastManager;

        import java.util.Locale;
        import java.util.Random;

/**
 * Created by Rob on 27/01/2018.
 */

public class DisturbService extends Service implements TextToSpeech.OnInitListener{

    private static NotificationManager mNotificationManager;
    private static int prevNotificationFilter = -1;

    private static TextToSpeech textToSpeech;

    private static Context appContext;

    private static String appName;

    private static Intent notifIntent;

    private static int drivNotiId = 0;

    private static SharedPreferences settings;
    private static SharedPreferences.Editor editPrefs;

    public void onCreate() {
        super.onCreate();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setLanguage(Locale.US);

        appContext = getApplicationContext();
        appName = getString(R.string.app_name);

        notifIntent = new Intent(this, DisturbService.class);

        settings = getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
        editPrefs = settings.edit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            if(intent.hasExtra("disable")) {
                userSelectedDoDisturb();

//            ChangeDNDService.cancelNotification(getApplicationContext(), intent.getIntExtra("notification_id", 0));

            /*Create a boolean that will stop thinking you're driving. What if the passenger is
            connected to the car's bluetooth? They click 'not driving' and it's automatically set again.*/
            }
        }
        return START_STICKY;
    }

    public static void userSelectedDoDisturb() {
        UtilitiesService.setUserNotDriving(true);
        doDisturb();
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

    public static void doNotDisturb() {
        if (mNotificationManager.isNotificationPolicyAccessGranted() && mNotificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
            prevNotificationFilter = mNotificationManager.getCurrentInterruptionFilter();

            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
            textToSpeech.speak("Turning on Do not Disturb.", TextToSpeech.QUEUE_ADD, null, Integer.toString(new Random().nextInt()));

            drivingNotification();

            UtilitiesService.setActive(true);

            sendToMainActivity(true);
        }

        if(settings.getBoolean("switchOtherApps", false)) {
            startOverlayService();
        }
    }
    public static void makeNoise() {
        Ringtone rTone = RingtoneManager.getRingtone(appContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        rTone.play();
    }

    public static void doDisturb() {
        if (mNotificationManager.isNotificationPolicyAccessGranted() && mNotificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
//            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);

            ChangeDNDService.cancelNotification(appContext, drivNotiId);
            sendToMainActivity(false);


            // sets interruption filter to what it used to be rather than always turning it off.
            if(prevNotificationFilter != -1){
                mNotificationManager.setInterruptionFilter(prevNotificationFilter);
            }
            else {
                mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            }
            textToSpeech.speak("Turning off Do not Disturb.", TextToSpeech.QUEUE_ADD, null, Integer.toString(new Random().nextInt()));

            DetectDrivingService.setSittingIntoCar(false);
            UtilitiesService.setActive(false);

            if(settings.getBoolean("switchOtherApps", false)) {
                stopOverlayService();
            }
        }
    }

    public static void startOverlayService() {
        Intent intent = new Intent(appContext, Overlay.class);
        appContext.startService(intent);
    }

    public static void stopOverlayService() {
        Intent intent = new Intent(appContext, Overlay.class);
        appContext.stopService(intent);
    }

    @Override
    public void onInit(int status) {

    }


    private static void drivingNotification() {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(appContext,  MainActivity.CHANNEL_ID);
        notification.setContentText("Do not Disturb Enabled as Driving.");
        notification.setSmallIcon( R.drawable.ic_stat_notify_driving );
        notification.setContentTitle( appName );

        notification.setVisibility(Notification.VISIBILITY_PUBLIC);

        notification.setColor(ContextCompat.getColor(appContext, R.color.cast_expanded_controller_ad_container_white_stripe_color));


        notifIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notifIntent.putExtra("disable", true);
        notifIntent.putExtra("notification_id", drivNotiId);
        PendingIntent disableIntent = PendingIntent.getService( appContext, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT );
        notification.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Not driving", disableIntent);

        NotificationManager notificationService = (NotificationManager)appContext.getSystemService(NOTIFICATION_SERVICE);
        notificationService.notify(0, notification.build());
    }

    // Necessary to change button check when enabled/disabled.
    private static void sendToMainActivity(boolean value) {
        Intent intent = new Intent("intentToggleButton");
        intent.putExtra("valueBool", value);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);
    }
}
