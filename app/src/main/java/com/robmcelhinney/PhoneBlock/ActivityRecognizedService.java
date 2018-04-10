package com.robmcelhinney.PhoneBlock;

/**
 * Created by Rob on 22/11/2017.
 */

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognizedService extends IntentService {

    public static final String ACTION_ActivityRecognizedService = "com.example.androidintentservice.RESPONSE";
    public static final String EXTRA_KEY_OUT_ACTIVITY = "EXTRA_OUT_ACTIVITY";
    public static final String EXTRA_KEY_OUT_CONFIDENCE = "EXTRA_OUT_ACTIVITY_CONFIDENCE";

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(ActivityRecognitionResult.hasResult(intent)){
            DetectedActivity detectedActivity = ActivityRecognitionResult.extractResult(intent).getMostProbableActivity();

            Intent intentResponse = new Intent();
            intentResponse.setAction(ACTION_ActivityRecognizedService);
            intentResponse.addCategory(Intent.CATEGORY_DEFAULT);
            intentResponse.putExtra(EXTRA_KEY_OUT_ACTIVITY, getActivityName(detectedActivity));
            intentResponse.putExtra(EXTRA_KEY_OUT_CONFIDENCE, Float.toString(detectedActivity.getConfidence()));
            sendBroadcast(intentResponse);
        }
    }

    private String getActivityName(DetectedActivity detectedActivity){
        switch (detectedActivity.getType()){
            case DetectedActivity.IN_VEHICLE:
                return "IN_VEHICLE";
            case DetectedActivity.ON_BICYCLE:
                return "ON_BICYCLE";
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.TILTING:
                return "TILTING";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            case DetectedActivity.ON_FOOT:
                return "ON_FOOT";
            case DetectedActivity.WALKING:
                return "WALKING";
            case DetectedActivity.UNKNOWN:
                return "UNKNOWN";
        }
        return "";
    }
}