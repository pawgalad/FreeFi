package com.hfad.freef;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Klasa ta dziedziczy po BroadcastReceiver,
 * co umozliwia jej nasluchiwanie w tle momentu, w
 * ktorym pozadana siec wifi staje sie dostepna.
 * W przypadku wykrycia dostepnosci generowane jest
 * powiadomienie wraz z uruchomieniem aplikacji po otworzeniu
 * powiadomienia.
 */
public class DetectConnection extends BroadcastReceiver {
    public static final int NOTIFICATION_ID = 5451;
    /**
     * session id sieci
     */
    public static final String SESSID = "AP";

    String TAG = getClass().getSimpleName();
    /**
     * kontekst aplikacji
     */
    private Context mContext;


    /**
     * metoda wywolywana gdy pojawi sie trigger w
     * postaci aktywnej sieci wifi o danym SESSID
     * @param context - kontekst aplikacji
     * @param intent  - intencja, ktora wywoluje te klase
     */
    @Override
    public void onReceive(Context context, Intent intent) {


        mContext = context;


        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI &&
                    networkInfo.isConnected()) {
                // Wifi is connected
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = wifiInfo.getSSID();

                if(ssid.contains(SESSID)){

                    Intent intent1 = new Intent(this.mContext, DisplayUsersActivity.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.mContext);
                    stackBuilder.addParentStack(DisplayUsersActivity.class);
                    stackBuilder.addNextIntent(intent1);
                    PendingIntent pendingIntent =
                            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification notification = new Notification.Builder(this.mContext)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Sieć WiFi jest dostępna")
                            .setAutoCancel(true)
                            .setPriority(Notification.PRIORITY_MAX)
                            .setDefaults(Notification.DEFAULT_VIBRATE)
                            .setContentIntent(pendingIntent)
                            .setContentText("Mozesz korzystac z aplikacji Aplikacja mobilna PRG")
                            .build();
                    NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(NOTIFICATION_ID, notification);



                }



            }
        }


    }





}