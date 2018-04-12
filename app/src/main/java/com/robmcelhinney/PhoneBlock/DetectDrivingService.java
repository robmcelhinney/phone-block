package com.robmcelhinney.PhoneBlock;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.round;

/**
 * Created by Rob on 27/01/2018.
 */

public class DetectDrivingService extends Service implements SensorEventListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private MyBroadcastReceiver myBroadcastReceiver;

    public GoogleApiClient mApiClient;

    private final int N_SAMPLES = 200;
    private final int N_SAMPLES_TOTAL_MAX = 1200;
    private final int N_CHECKS = 20;

    private List<Float> x;
    private List<Float> y;
    private List<Float> z;

    private List<Float> x1;
    private List<Float> y1;
    private List<Float> z1;

    private List<Float> data;

    private int numTimesOnFoot = 0;
    private int numTimesCheckBT = 0;

    public boolean isSittingIntoCar() {
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

    private IntentFilter intentFilter;

    public void onCreate() {
        super.onCreate();

        x = new ArrayList<>();
        y = new ArrayList<>();
        z = new ArrayList<>();

        settings = getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);

        myBroadcastReceiver = new MyBroadcastReceiver();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //register BroadcastReceiver for ActivityRecognizedService
        intentFilter = new IntentFilter(ActivityRecognizedService.ACTION_ActivityRecognizedService);
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
        registerReceiver(myBroadcastReceiver, intentFilter);
        return START_STICKY;
//        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(myBroadcastReceiver);
        super.onDestroy();
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

        if(settings.getBoolean("switchBT", false)) {
            if (x.size() > N_SAMPLES_TOTAL_MAX && y.size() > N_SAMPLES_TOTAL_MAX && z.size() > N_SAMPLES_TOTAL_MAX) {
                x.subList(0, N_SAMPLES).clear();
                y.subList(0, N_SAMPLES).clear();
                z.subList(0, N_SAMPLES).clear();
            }
        }
        else {
            activityPrediction();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        PendingIntent pendingIntent = PendingIntent.getService( getApplicationContext(), 0, new Intent( getApplicationContext(), ActivityRecognizedService.class ), PendingIntent.FLAG_UPDATE_CURRENT );
        mActivityRecognitionClient.requestActivityUpdates(3000, pendingIntent);
        // TODO: Change from 3 seconds to 5.
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
            float conf = Float.parseFloat(intent.getStringExtra(ActivityRecognizedService.EXTRA_KEY_OUT_CONFIDENCE));

            if (activity.equalsIgnoreCase("ON_FOOT") && conf > 0.9){
                if (isSittingIntoCar()){
                    setSittingIntoCar(false);
                }

                if (!onFoot) {
                    if(settings.getBoolean("switchkey", false)){
                        onResume();
                    }
                    onFoot = true;
                }

                if(UtilitiesService.isUserNotDriving()){
                    UtilitiesService.setUserNotDriving(false);
                }

                // Will not deactive block until user has been on foot for ~10 seconds.
                // This prevents a single poor prediction turning off the block.
                if(UtilitiesService.isActive() && numTimesOnFoot >= 2) {
                    DisturbService.doDisturb();
                }

                numTimesOnFoot++;
                numTimesCheckBT = 0;
            }
            else {
                if((activity.equalsIgnoreCase("IN_VEHICLE") || activity.equalsIgnoreCase("STILL")) && onFoot) {
                    displayToast("Activity: " + activity + ". onFoot: " + onFoot);
                    activityPrediction();
                    if(settings.getBoolean("switchkey", false)) {
                        onPause();
                    }
                }
                if(activity.equalsIgnoreCase("IN_VEHICLE") && conf > 0.95 && isSittingIntoCar()
                        && !UtilitiesService.isActive()) {
                    DisturbService.doNotDisturb();
                }
                if (onFoot && !(activity.equalsIgnoreCase("TILTING") || activity.equalsIgnoreCase("UNKNOWN"))) {
                    if(settings.getBoolean("switchkey", false)) {
                        onPause();
                    }
                    onFoot = false;
                }
                numTimesOnFoot = 0;

                //Testing Bluetooth in Car.
                if (activity.equalsIgnoreCase("IN_VEHICLE") && conf > 0.90 && !UtilitiesService.isActive()){
                    if(numTimesCheckBT <= 6) {
                        numTimesCheckBT++;
                        checkBluetooth();
                    }
                    else{
                        numTimesCheckBT = 0;
                    }
                }
            }
            sendMessageToActivity("currText", activity);
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
                    if ((deviceClass == BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO || deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE)) {
                        DisturbService.doNotDisturb();
                        onPause();
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

    private void displayToast(final String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void activityPrediction() {
//        makeNoise();

        if (settings.getBoolean("switchBT", false)) {
            displayToast("X1 size: " + x.size() + ". Predicted Loops: " + (x.size() - N_SAMPLES) / N_CHECKS);
            int loops = 0;
            float[] results;
            while (x.size() >= N_SAMPLES && y.size() >= N_SAMPLES && z.size() >= N_SAMPLES) {
                x1 = new ArrayList(x.subList(0, N_SAMPLES));
                y1 = new ArrayList(y.subList(0, N_SAMPLES));
                z1 = new ArrayList(z.subList(0, N_SAMPLES));
                if (x1.size() == N_SAMPLES && y1.size() == N_SAMPLES && z1.size() == N_SAMPLES) {
                    loops++;
                    data = new ArrayList<>();
                    data.addAll(x1);
                    data.addAll(y1);
                    data.addAll(z1);

                    results = classifier.predictProbabilities(toFloatArray(data));

                    sittingcarValue = round(results[2]);
                    sendMessageToActivity("sittingCarText", Float.toString(sittingcarValue));

                    // Will Delete further down the line.
                    if (sittingcarValue > greatestProbValue) {
                        greatestProbValue = sittingcarValue;
                        sendMessageToActivity("greatestProb", String.valueOf(greatestProbValue));
                    }
                    //End deletion.

                    if (sittingcarValue > 0.85) {
//                            displayToast("Sitting into car is" + sittingcarValue);
                        setSittingIntoCar(true);
                            makeNoise();
                            displayToast("Break out of loop");
                        break;
                    }
                }
                x.subList(0, 20).clear();
                y.subList(0, 20).clear();
                z.subList(0, 20).clear();
            }

            x.clear();
            y.clear();
            z.clear();
            displayToast("done activityprediction! Loops: " + loops);

        } else {
            if (x.size() == N_SAMPLES && y.size() == N_SAMPLES && z.size() == N_SAMPLES && (x.size() % 20 == 0 && y.size() % 20 == 0 && z.size() % 20 == 0)) {
                data = new ArrayList<>();
                data.addAll(x);
                data.addAll(y);
                data.addAll(z);

                float[] results = classifier.predictProbabilities(toFloatArray(data));

                sittingcarValue = round(results[2]);
                sendMessageToActivity("sittingCarText", Float.toString(sittingcarValue));


                // Will Delete further down the line.
                if (greatestProbValue < sittingcarValue) {
                    greatestProbValue = sittingcarValue;
                    //                greatestProb.setText(String.valueOf(greatestProbValue));
                    sendMessageToActivity("greatestProb", String.valueOf(greatestProbValue));
                }
                //End deletion.


                if (sittingcarValue > 0.85) {
                    sittingIntoCar = true;
                    makeNoise();
                    onPause();
                }

                x.subList(0, 20).clear();
                y.subList(0, 20).clear();
                z.subList(0, 20).clear();
            }
        }
    }

    public float[] toFloatArray(List<Float> list1) {
        int i = 0;
        float[] array = new float[list1.size()];

        for (Float f : list1) {
            array[i++] = (f != null ? f : Float.NaN);
        }
        return array;
    }

    public void makeNoise() {
        Ringtone rTone = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        rTone.play();
    }
    public void makeNoise2() {
        Ringtone rTone = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        rTone.play();
    }


    private void sendMessageToActivity(String type, String msg) {
        Intent intent = new Intent("intentKey");
        intent.putExtra(type, true);
        intent.putExtra("text", msg);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
