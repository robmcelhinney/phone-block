package com.example.rob.FYPDrivingApp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognitionClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.*;

import static java.lang.Math.round;
import java.math.BigDecimal;

import com.google.android. gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;

/**
 * Created by Rob on 27/01/2018.
 */

public class DetectDrivingService extends Service implements SensorEventListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private MyBroadcastReceiver myBroadcastReceiver;

    public GoogleApiClient mApiClient;

    private NotificationManager mNotificationManager;

    private final int N_SAMPLES = 200;
    private final int N_SAMPLES_TOTAL = 700;
    private final int N_CHECKS = 20;

    private List<Float> x;
    private List<Float> y;
    private List<Float> z;

    private List<Float> data;

    public static boolean isSittingIntoCar() {
        return sittingIntoCar;
    }

    public static void setSittingIntoCar(boolean newSittingIntoCar) {
        sittingIntoCar = newSittingIntoCar;
    }

    private static boolean sittingIntoCar = false;
    private boolean onFoot = false;

    private ActivityRecognitionClient mActivityRecognitionClient;

    private BluetoothAdapter mBluetoothAdapter;

    private TensorFlowClassifier classifier;

    private float sittingcarValue = 0;
    private float greatestProbValue = 0;

    private SharedPreferences settings;

//    SharedPreferences prefs;
//    SharedPreferences.Editor editPrefs;

    public void onCreate() {
        super.onCreate();

        x = new ArrayList<>();
        y = new ArrayList<>();
        z = new ArrayList<>();

        settings = getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);

        myBroadcastReceiver = new MyBroadcastReceiver();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //register BroadcastReceiver for ActivityRecognizedService
        IntentFilter intentFilter = new IntentFilter(ActivityRecognizedService.ACTION_ActivityRecognizedService);
        intentFilter.addAction(ActivityRecognizedService.ACTION_ActivityRecognizedService);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver, intentFilter);

        classifier = new TensorFlowClassifier(getApplicationContext());

        mActivityRecognitionClient = new ActivityRecognitionClient(getApplicationContext());

        mApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();

//        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        editPrefs = prefs.edit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
//        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadcastReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        x.add(event.values[0]);
        y.add(event.values[1]);
        z.add(event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( getApplicationContext(), ActivityRecognizedService.class );
        PendingIntent pendingIntent = PendingIntent.getService( getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        mActivityRecognitionClient.requestActivityUpdates(3000, pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {

    }


    // Receiving data back from ActivityRecognizedService
    // Receives the activity and confidence
    public class MyBroadcastReceiver extends BroadcastReceiver implements SensorEventListener, GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
        @Override
        public void onReceive(Context context, Intent intent) {
            String activity = intent.getStringExtra(ActivityRecognizedService.EXTRA_KEY_OUT_ACTIVITY);
            String confidence = intent.getStringExtra(ActivityRecognizedService.EXTRA_KEY_OUT_CONFIDENCE);
            float conf = Float.parseFloat(confidence);




            if(activity.equalsIgnoreCase("STILL")) {
//                DisturbService.doNotDisturb();
                checkBluetooth();
            }



            if (activity.equalsIgnoreCase("ON_FOOT") && conf > 0.9){
                if (isSittingIntoCar()){setSittingIntoCar(false);}

                if (!onFoot) {
                    onResume();
                    onFoot = true;
                }

                if(UtilitiesService.isActive()) {
                    DisturbService.doDisturb();
                }
            }
            else {
                if((activity.equalsIgnoreCase("IN_VEHICLE") || activity.equalsIgnoreCase("STILL")) && onFoot) {
                    activityPrediction();
                }

                if(activity.equalsIgnoreCase("IN_VEHICLE") && conf > 0.95 && isSittingIntoCar()
                        && !UtilitiesService.isActive()) {
                    DisturbService.doNotDisturb();
                }
                if (onFoot) {
                    onPause();
                    onFoot = false;
                }
            }
            sendMessageToActivity("currText", activity);


            //Testing Bluetooth in Car.
            if (activity.equalsIgnoreCase("IN_VEHICLE")){
                checkBluetooth();
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {

        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }

        @Override
        public void onResult(@NonNull Status status) {

        }
    }

    private void checkBluetooth() {
        if(settings.getBoolean("switchBT", false) && !UtilitiesService.isUserNotDriving() && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
                && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {
            for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
                BluetoothClass bluetoothClass = device.getBluetoothClass();
                if (bluetoothClass != null) {
                    int deviceClass = bluetoothClass.getDeviceClass();
                    sendMessageToActivity("BTText", deviceClass + " :( " + BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO);
                    if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO || deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE) {
//                    if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO) {

//                                BTtextView.setText(bluetoothClass.getMajorDeviceClass());
//                                BTtextView.setText(deviceClass + " : " + BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO);

                        sendMessageToActivity("BTText", deviceClass + " :)Car " + BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO + " :)Handsfree " + BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE);

                        if(!UtilitiesService.isActive()) {
                            DisturbService.doNotDisturb();
                        }
                    }
                }
            }
        }
    }

    private void onPause() {
        getSensorManager().unregisterListener(this);
    }

    private void onResume() {
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }

    private SensorManager getSensorManager() {
        return (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    private void activityPrediction() {
        if(x.size() > N_SAMPLES_TOTAL && y.size() > N_SAMPLES_TOTAL && z.size() > N_SAMPLES_TOTAL) {
            x.subList(0, x.size() - N_SAMPLES_TOTAL).clear();
            y.subList(0, x.size() - N_SAMPLES_TOTAL).clear();
            z.subList(0, x.size() - N_SAMPLES_TOTAL).clear();
        }
        if (x.size() >= N_SAMPLES && y.size() >= N_SAMPLES && z.size() >= N_SAMPLES) {
            for(int i = 0; i < x.size() / N_CHECKS; i++) {
                data = new ArrayList<>();
                data.addAll(x.subList(0, N_SAMPLES+1));
                data.addAll(y.subList(0, N_SAMPLES+1));
                data.addAll(z.subList(0, N_SAMPLES+1));

                float[] results = classifier.predictProbabilities(toFloatArray(data));

                sittingcarValue = round(results[2]);
                sendMessageToActivity("sittingCarText", Float.toString(sittingcarValue));

                // Will Delete further down the line.
                if(greatestProbValue < sittingcarValue) {
                    greatestProbValue = sittingcarValue;
                    sendMessageToActivity("greatestProb", String.valueOf(greatestProbValue));
                }
                //End deletion.


                if(sittingcarValue > 0.85){
                    String sittingCarString = "Sitting into car is" + sittingcarValue;
                    setSittingIntoCar(true);
                    makeNoise();
                    break;
                }

                x.subList(0, N_CHECKS).clear();
                y.subList(0, N_CHECKS).clear();
                z.subList(0, N_CHECKS).clear();
            }
            x.clear();
            y.clear();
            z.clear();
        }
    }

    private float[] toFloatArray(List<Float> list) {
        int i = 0;
        float[] array = new float[list.size()];

        for (Float f : list) {
            array[i++] = (f != null ? f : Float.NaN);
        }
        return array;
    }

    public void makeNoise() {
        Ringtone rTone = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        rTone.play();
    }


    private void sendMessageToActivity(String type, String msg) {
        Intent intent = new Intent("intentKey");
        intent.putExtra(type, true);
        intent.putExtra("text", msg);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}