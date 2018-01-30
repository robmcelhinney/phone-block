package com.example.rob.FYPDrivingApp;

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognitionClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.round;

/**
 * Created by Rob on 27/01/2018.
 */

public class detectDrivingService extends Service {

    MyBroadcastReceiver myBroadcastReceiver;

    public GoogleApiClient mApiClient;

    private NotificationManager mNotificationManager;

    private static final int N_SAMPLES = 200;
    private static List<Float> x;
    private static List<Float> y;
    private static List<Float> z;

    private boolean sittingIntoCar = false;
    private boolean onFoot = false;

    private ActivityRecognitionClient mActivityRecognitionClient;

    private BluetoothAdapter mBluetoothAdapter;

    public void onCreate() {
        super.onCreate();


        myBroadcastReceiver = new MyBroadcastReceiver();

        //register BroadcastReceiver for ActivityRecognizedService
        IntentFilter intentFilter = new IntentFilter(ActivityRecognizedService.ACTION_ActivityRecognizedService);
        intentFilter.addAction(ActivityRecognizedService.ACTION_ActivityRecognizedService);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver, intentFilter);
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
                DisturbService.doNotDisturb();
            }


            if (activity.equalsIgnoreCase("ON_FOOT") && conf > 0.9){
                makeNoise();

                if (sittingIntoCar){sittingIntoCar = false;}

                if (!onFoot) {
                    onResume();
                    onFoot = true;
                }

                doNotDisturb();
            }
            else {
                if(activity.equalsIgnoreCase("IN_VEHICLE") && conf > 0.9 && sittingIntoCar) {
                    doNotDisturb();
                }
                if (onFoot) {
                    onPause();
                    onFoot = false;
                }
            }
            currText.setText(activity);


            //Testing Bluetooth in Car.
            if (activity.equalsIgnoreCase("IN_VEHICLE")){
                if(mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
                        && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {
                    for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
                        BluetoothClass bluetoothClass = device.getBluetoothClass();
                        if (bluetoothClass != null) {
                            int deviceClass = bluetoothClass.getDeviceClass();
                            if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO) {
//                                BTtextView.setText(bluetoothClass.getMajorDeviceClass());
                                BTtextView.setText(deviceClass + " : " + BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO);
                                doNotDisturb();
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            activityPrediction();
            x.add(event.values[0]);
            y.add(event.values[1]);
            z.add(event.values[2]);
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

    protected void onPause() {
        getSensorManager().unregisterListener(this);
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }


    private void activityPrediction() {
        if (x.size() == N_SAMPLES && y.size() == N_SAMPLES && z.size() == N_SAMPLES && (x.size() % 20 == 0 && y.size() % 20 == 0 && z.size() % 20 == 0)) {
            List<Float> data = new ArrayList<>();
            data.addAll(x);
            data.addAll(y);
            data.addAll(z);

            float[] results = classifier.predictProbabilities(toFloatArray(data));

            sittingcarValue = round(results[2]);
            sittingcarTextView.setText(Float.toString(sittingcarValue));


            // Will Delete further down the line.
            if(greatestProbValue < sittingcarValue) {
                greatestProbValue = sittingcarValue;
                greatestProb.setText(String.valueOf(greatestProbValue));
            }
            //End deletion.


            if(sittingcarValue > 0.85){
                String sittingCarString = "Sitting into car is" + sittingcarValue;
                sittingIntoCar = true;
            }

            x.subList(0, 20).clear();
            y.subList(0, 20).clear();
            z.subList(0, 20).clear();
        }
    }

    public void makeNoise() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
    }
}
