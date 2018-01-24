package com.example.rob.FYPDrivingApp;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
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
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.*;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;

public class MainActivity extends AppCompatActivity implements SensorEventListener,
        TextToSpeech.OnInitListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>
{

    public GoogleApiClient mApiClient;

    private NotificationManager mNotificationManager;

    private MyBroadcastReceiver myBroadcastReceiver;

    private static final int N_SAMPLES = 200;
    private static List<Float> x;
    private static List<Float> y;
    private static List<Float> z;
    private TextToSpeech textToSpeech;
    private TensorFlowClassifier classifier;

    private float sittingcarValue = 0;
    private float greatestProbValue = 0;

    private TextView greatestProb;
    private TextView sittingcarTextView;
    private TextView currText;
    private TextView BTtextView;
    private boolean sittingIntoCar = false;
    private boolean onFoot = false;
    private int prevNotificationFilter;

    private ActivityRecognitionClient mActivityRecognitionClient;

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        x = new ArrayList<>();
        y = new ArrayList<>();
        z = new ArrayList<>();

        greatestProb = (TextView) findViewById(R.id.greatestProb);
        sittingcarTextView = (TextView) findViewById(R.id.sittingcar_prob);
        currText = (TextView) findViewById(R.id.currText);
        BTtextView = (TextView) findViewById(R.id.BTtextView);

        classifier = new TensorFlowClassifier(getApplicationContext());

        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setLanguage(Locale.US);

        myBroadcastReceiver = new MyBroadcastReceiver();

        //register BroadcastReceiver for ActivityRecognizedService
        IntentFilter intentFilter = new IntentFilter(ActivityRecognizedService.ACTION_ActivityRecognizedService);
        intentFilter.addAction(ActivityRecognizedService.ACTION_ActivityRecognizedService);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver, intentFilter);

        mActivityRecognitionClient = new ActivityRecognitionClient(this);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void onInit(int status) {

    }

    protected void onPause() {
        getSensorManager().unregisterListener(this);
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
        getSensorManager().registerListener(this, getSensorManager().getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        activityPrediction();
        x.add(event.values[0]);
        y.add(event.values[1]);
        z.add(event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

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
//                textToSpeech.speak(sittingCarString, TextToSpeech.QUEUE_ADD, null, Integer.toString(new Random().nextInt()));
                sittingIntoCar = true;
            }

            x.subList(0, 20).clear();
            y.subList(0, 20).clear();
            z.subList(0, 20).clear();
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

    private static float round(float d) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    private SensorManager getSensorManager() {
        return (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, ActivityRecognizedService.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        mActivityRecognitionClient.requestActivityUpdates(3000, pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onResult(Status status) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //un-register BroadcastReceiver
        unregisterReceiver(myBroadcastReceiver);
    }

    // Receiving data back from ActivityRecognizedService
    // Receives the activity and confidence
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String activity = intent.getStringExtra(ActivityRecognizedService.EXTRA_KEY_OUT_ACTIVITY);
            String confidence = intent.getStringExtra(ActivityRecognizedService.EXTRA_KEY_OUT_CONFIDENCE);
            float conf = Float.parseFloat(confidence);







            if(activity.equalsIgnoreCase("STILL")) {
                doNotDisturb();
            }








            if (activity.equalsIgnoreCase("ON_FOOT") && conf > 0.9){
                makeNoise();

                if (sittingIntoCar){sittingIntoCar = false;}

                if (!onFoot) {
                    onResume();
                    onFoot = true;
                }
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
                                textToSpeech.speak("Connected to Car Bluetooth.", TextToSpeech.QUEUE_ADD, null, Integer.toString(new Random().nextInt()));
                                doNotDisturb();
                            }
                        }
                    }
                }
            }
        }
    }

    private void doNotDisturb() {
        if (mNotificationManager.isNotificationPolicyAccessGranted() && mNotificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
            prevNotificationFilter = mNotificationManager.getCurrentInterruptionFilter();

            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
            textToSpeech.speak("Turning on Do not Disturb.", TextToSpeech.QUEUE_ADD, null, Integer.toString(new Random().nextInt()));

            drivingNotification();
        }
    }

    private void doDisturb() {
        if (mNotificationManager.isNotificationPolicyAccessGranted() && mNotificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
//            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);

            // sets interruption filter to what it used to be rather than always turning it off.
            mNotificationManager.setInterruptionFilter(prevNotificationFilter);
            textToSpeech.speak("Turning off Do not Disturb.", TextToSpeech.QUEUE_ADD, null, Integer.toString(new Random().nextInt()));
        }
    }

    public void makeNoise() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.hasExtra("disable")) {
            doDisturb();

            NotificationManager notificationManager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(intent.getIntExtra("notification_id", 0));


            /*Create a boolean that will stop thinking you're driving. What if the passenger is
            connected to the car's bluetooth? They click 'not driving' and it's automatically set again.*/
        }
    }


    public static void cancelNotification(Context context, int notifyId) {
        NotificationManager notiMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notiMgr.cancel(notifyId);
    }
}
