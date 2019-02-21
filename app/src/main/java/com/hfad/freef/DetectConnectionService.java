package com.hfad.freef;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;


/**
 * Usluga, ktora rejestruje BroadcastReceiver klasy DetectConnection.
 * Usluga ta zapewnia ciaglosc dzialania aplikacji.
 */
public class DetectConnectionService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(new DetectConnection(), new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        return Service.START_STICKY;
    }

    @Override
    public void onLowMemory() {
        stopSelf();
        super.onLowMemory();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
