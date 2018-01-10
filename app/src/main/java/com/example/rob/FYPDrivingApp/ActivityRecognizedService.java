package com.example.rob.FYPDrivingApp;

/**
 * Created by Rob on 22/11/2017.
 */

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognizedService extends IntentService {

    public static final String ACTION_ActivityRecognizedService = "com.example.androidintentservice.RESPONSE";
    public static final String EXTRA_KEY_OUT_ACTIVITY = "EXTRA_OUT_ACTIVITY";
    public static final String EXTRA_KEY_OUT_CONFIDENCE = "EXTRA_OUT_ACTIVITY_CONFIDENCE";
    String extraOutAct;
    String extraOutConf;

    private NotificationManager mNotificationManager;

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }

    Handler mHandler;

    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
    }

    private TextView currText;

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)){
            ActivityRecognitionResult activityRecognitionResult = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity detectedActivity = activityRecognitionResult.getMostProbableActivity();
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            int confidence = detectedActivity.getConfidence();
            String recognizeActivity = getActivityName(detectedActivity);

            Log.d("ActivityRecogition","Confidence : " + confidence);
            Log.d("ActivityRecogition","RecognizeActivity : " + recognizeActivity);

//            final String sText = recognizeActivity;
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(ActivityRecognizedService.this, sText, Toast.LENGTH_LONG).show();
//                }
//            });

            extraOutAct = recognizeActivity;
            extraOutConf = Float.toString(confidence);


            Intent intentResponse = new Intent();
            intentResponse.setAction(ACTION_ActivityRecognizedService);
            intentResponse.addCategory(Intent.CATEGORY_DEFAULT);
            intentResponse.putExtra(EXTRA_KEY_OUT_ACTIVITY, extraOutAct);
            intentResponse.putExtra(EXTRA_KEY_OUT_CONFIDENCE, extraOutConf);
            sendBroadcast(intentResponse);
        }
    }

    private String getActivityName(DetectedActivity detectedActivity){
        switch (detectedActivity.getType()){
            case DetectedActivity.IN_VEHICLE:
                // enter here after tensorflow thinks you're in car
                if(detectedActivity.getConfidence() > 0.9) {
                    displayNotification("Are you on driving?");
//                    if (mNotificationManager.isNotificationPolicyAccessGranted()) {
//                        Log.i("currentInterruptionFil", String.valueOf(mNotificationManager.getCurrentInterruptionFilter()));
//                        if (mNotificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_NONE) {
//                            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
//                            Toast.makeText(getApplicationContext(), "Do Not Disturb", Toast.LENGTH_SHORT)
//                                    .show();
//                        }
//                    }
                }
                return "IN_VEHICLE";
            case DetectedActivity.ON_BICYCLE:
                return "ON_BICYCLE";
            case DetectedActivity.STILL:
//                displayNotification("Are you still?");
                return "STILL";
            case DetectedActivity.TILTING:
                return "TILTING";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            case DetectedActivity.ON_FOOT:
                // Check
                if(detectedActivity.getConfidence() > 0.80) {
                    //start checking with tensorflow
                }
//                displayNotification("Are you on foot?");
                return "ON_FOOT";
            case DetectedActivity.WALKING:
//                displayNotification("Are you on walking?");
                return "WALKING";
            case DetectedActivity.UNKNOWN:
                return "UNKNOWN";
        }
        return "";
    }

    private void displayNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentText(message);
        builder.setSmallIcon( R.mipmap.ic_launcher );
        builder.setContentTitle( getString( R.string.app_name ) );
        NotificationManagerCompat.from(this).notify(0, builder.build());
    }
}