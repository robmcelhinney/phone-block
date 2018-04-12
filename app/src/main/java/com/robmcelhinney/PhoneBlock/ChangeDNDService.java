package com.robmcelhinney.PhoneBlock;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import java.util.Objects;

public class ChangeDNDService extends Service {
    private BroadcastReceiver doNotDisturbBroadcastReceiver;
    private IntentFilter intentChangingDND;

    public void onCreate() {
        super.onCreate();

        doNotDisturbBroadcastReceiver = new doNotDisturbBroadcastReceiver();
        intentChangingDND = new IntentFilter();
        intentChangingDND.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
        registerReceiver(doNotDisturbBroadcastReceiver, intentChangingDND);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        intentChangingDND.addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
        registerReceiver(doNotDisturbBroadcastReceiver, intentChangingDND);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(doNotDisturbBroadcastReceiver);
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
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                assert mNotificationManager != null;
                if (mNotificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_NONE) {
                    DisturbService.cancelNotification();
                    DisturbService.doDisturb();
                }
            }
        }
    }
}