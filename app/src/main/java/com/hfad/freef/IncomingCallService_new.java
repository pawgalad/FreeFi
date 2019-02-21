package com.hfad.freef;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;

/**
 *BroadcastReceiver oczekujacy na przychodzace polaczenia.
 * W klasie DisplayUsersActivity obiekt tej klasy zostal zarejestrowany
 * i powizany z filtrem intencji.
 */

public class IncomingCallService_new extends BroadcastReceiver {

    public static Intent incomingIntent;
    /**
     *Metoda reagujaca na konkretna intencje.
     */

    @Override
    public void onReceive(Context context, Intent intent) {

        final DisplayUsersActivity callActivity_new = (DisplayUsersActivity) context;
        String incoming = "incoming";
        incomingIntent = intent;
        SipAudioCall incomingCall = null;
        Intent goToCall = new Intent(context, CallActivity_new.class);

        context.startActivity(goToCall);

    }
}

