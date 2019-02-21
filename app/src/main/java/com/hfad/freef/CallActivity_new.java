package com.hfad.freef;

import android.content.pm.ActivityInfo;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;


/**
 * Aktywnosc ta obługuje przychodzace połączenia jak też realizuje połączenia wychodzące
 * Informacja "kto dzwoni" pobierana jest z obiektu call podczas inicjalizacji tej aktynowści.
 * Informacja o użytkowniku docelowym jest  przechowywana w intencji, ktora jest uruchamiana w klasie
 * DisplayUsersActivity. Intencja ta uruchamia aktywność CallAcitvity_new.

 */
public class CallActivity_new extends AppCompatActivity {

    //Aktywność zarówno dla Incoming i Outgoing, zmieniać się będą przyciski

    //Views
    private TextView call_status;
    /**
     * timer czasu trwania polaczenia
     */
    private TextView call_timer;
    /**
     * nazwa uzytkownika od ktorego
     * przychodzi polaczenie

     */
    public TextView caller_name;
    /**
     * odrzucenie polaczenia

     */
    private ImageButton deny_call_btn;
    /**
     * akceptacja polaczenia

     */
    private ImageButton accept_call_btn;
    /**
     * przycisk umozliwiajacy wlaczenie/wylaczenie
     * mikrofonu
     */
    private ImageButton speaker_btn;


    /**
     * mikrofon wlaczony

     */
    private boolean speakerOn = false;

    public static final String CALL_ID = "call_id";

    //SIP

    /**
     * adres serwera

     */
    private static final String DOMAIN = DisplayUsersActivity.SERVER;
    ;



    /**
     * obiekt call niezbedny do wykonywania/odbierania polaczen

     */
    public SipAudioCall call;

    /**
     * manager - SipManager

     */
    public SipManager manager = DisplayUsersActivity.manager;
    /**
     * Profil Sip zarejestrowanego uzytkownika

     */
    public SipProfile localProfile = DisplayUsersActivity.localProfile;

    /**
     * status polaczenia

     */
    private TextView statusText;

    //Intencje
    /**
     * aktywnosc zostala uruchomiona z wykorzystaniem intencji
     * z powodu przychodzacego polaczenia.

     */
    public static final String CALL_INTENT = "incomingCall"; //false - Outgoing (to z ActiveUsers), True - Incoming (to z IncomingService)
    /**
     * typ polaczenia - wychodzace/przychodzace

     */
    private boolean callType = true;
    /**
     *  zmienna pomocnicza

     */
    public static final String SIP_NAME_TO_CALL = "SIP_NAME_TO_CALL";
    /**
     *  nazwa uzytkownika docelowego

     */
    private String sipNameOutcome = null;
    /**
     * nazwa uzytkownika, od ktorego przychodzi polaczenie

     */
    private String sipNameIncome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_call_new);

        call_status = (TextView) findViewById(R.id.call_status);
        call_timer = (TextView) findViewById(R.id.call_timer);
        deny_call_btn = (ImageButton) findViewById(R.id.deny_call_button);
        accept_call_btn = (ImageButton) findViewById(R.id.accept_call_button);
        speaker_btn = (ImageButton) findViewById(R.id.btnSpeaker);
        caller_name = (TextView) findViewById(R.id.caller_name);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);


        //tutaj sobie odbieramy intencje z klasy ActiveUsers lub z IncomingCallService
        Bundle bundle = getIntent().getExtras();
//

        if(bundle!=null) {
            callType = false;

            if (bundle.containsKey(SIP_NAME_TO_CALL))
                sipNameOutcome = bundle.getString(SIP_NAME_TO_CALL);

        }else{
            prepareCallObject();
        }
        initializeViews();
        try {
            if(!callType){
                makeCall();
                callType = !callType;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * odrzucenie/zakonczenie polaczenia

         */
        deny_call_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                endCall();
                finish();

            }

        });



        /**
         * akceptacja polaczenia

         */
        accept_call_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                        try{
//                            answerCall();
                            call.answerCall(30);
                            call_timer.setVisibility(TextView.VISIBLE);
                            call.startAudio();
                            call.setSpeakerMode(true);
                            if (call.isMuted()) call.toggleMute();
                        }catch (SipException e){
                            e.printStackTrace();
                        }
            }
        });
    }

    /**
     * inicjalizacja widoku w zalenosci od typu placzenia.
     * Jesli polaczenie jest przychodzace, dostepny
     * powinien byc button pozwalajcy odrzucic polaczenie
     * lub je odebrac.


     */
    private void initializeViews(){


        if(callType){
            accept_call_btn.setVisibility(ImageButton.VISIBLE);
            deny_call_btn.setVisibility(ImageButton.VISIBLE);

        }
        else{
            accept_call_btn.setVisibility(ImageButton.GONE);
            deny_call_btn.setVisibility(ImageButton.VISIBLE);
            caller_name.setText(sipNameOutcome);
        }

    }



    /**
     * Metoda dziala w przypadku polaczenia przychodzacego.
     * Ustawiamy na obiekcie call listener w celu
     * sprawdzenia kto dzwoni, zanim jeszcze polaczenie
     * zostanie odebrane.

     */
    public void prepareCallObject(){

        SipAudioCall.Listener listener = new SipAudioCall.Listener(){


            @Override
            public void onCallEstablished(SipAudioCall call) {
//                call.startAudio();
//                call.setSpeakerMode(true);
                Log.i("sip-->", "onCallEstablished");


                if (mTotalTime == 0L){
                    mPointTime = System.currentTimeMillis();
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    mHandler.postDelayed(mUpdateTimeTask, 100);
                }
            }


            @Override
            public void onCallEnded(SipAudioCall call) {

                mHandler.removeCallbacks(mUpdateTimeTask);
                finish();

//                endCall();

            }

        };
        try {
            call = manager.takeAudioCall(IncomingCallService_new.incomingIntent, listener);

            sipNameIncome = call.getPeerProfile().getUserName();
//            updateStatus("Odebrano od " + sipNameIncome);
            caller_name.setText(sipNameIncome);
        } catch (SipException e) {
            e.printStackTrace();
        }


    }

    /**
     * makeCall - metoda realizuje polaczenia wychodzace.
     *  Korzysta z listenera ustawionego na obieckie call.
     *  Informacje o adresacie polaczenia pobierane sa
     *  z obiektu bunlde, podczas uruchomienia tej aktywnosci.

     */

    public void makeCall() {

//     updateStatus("Dzwonię do " + sipNameOutcome);
        call_timer.setVisibility(TextView.VISIBLE);

        try {
            SipAudioCall.Listener callListener = new SipAudioCall.Listener() {
                @Override
                public void onCallEstablished(SipAudioCall call) {

                    call.startAudio();
                    call.setSpeakerMode(true);
                    Log.i("sip-->", "onCallEstablished");



                    if (mTotalTime == 0L){
                        mPointTime = System.currentTimeMillis();
                        mHandler.removeCallbacks(mUpdateTimeTask);
                        mHandler.postDelayed(mUpdateTimeTask, 100);
                    }
                }
                @Override
                public void onError(SipAudioCall call, int errorCode, String errorMessage){
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    finish();
                    endCall();
                }

                @Override
                public void onCallEnded (SipAudioCall call) {
//                    updateStatus("Gotowy");
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    finish();
//                    endCall();
                }


            };

            call = manager.makeAudioCall(localProfile.getUriString(), "sip:" + sipNameOutcome + "@" + DOMAIN, callListener, 30);


        } catch (Exception e ) {
            Log.d("Call initiation error.", "Error when trying to make a call. " + e);
            if (localProfile != null) {
                try {
                    manager.close(localProfile.getUriString());
                } catch (Exception ee) {
                    Log.d("Manager closing error.", "Error when trying to close manager.", ee);
                    ee.printStackTrace();
                }
            }
            if(call != null) {
                call.close();
            }
        }
    }
    /**
     * Metoda obslugujaca proces konczacy
     * polaczenie. Wykorzystuje sie
     * ten sam obiekt call.

     */
    public void endCall() {
        if (call != null) {
            try {
                call.endCall();
                finish();

            } catch (SipException se) {
                Log.d("Call ending error", "Error when trying to end a call.", se);
            }
            call.close();

        }
    }



    public void updateStatus (final String status) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                call_status.setText(status);
            }
        });
    }

    // ==========Timer==============
    private long mPointTime = 0;
    private long mTotalTime = 0;
    private Handler mHandler = new Handler();
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            Log.i("sip -->", "CallConnected run  ");
            mTotalTime += System.currentTimeMillis() - mPointTime;
            mPointTime = System.currentTimeMillis();
            int seconds = (int) (mTotalTime / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            if (seconds < 10) {
                call_timer.setText("" + minutes + ":0" + seconds);
            } else {
                call_timer.setText("" + minutes + ":" + seconds);
            }

            mHandler.postDelayed(this, 1000);
        }
    };

    public void changeSpeakerStatus(View view) {

        if (speakerOn) {
            speaker_btn.setImageResource(R.drawable.fm_mute);
        } else {
            speaker_btn.setImageResource(R.drawable.fm_unmute);
        }
        speakerOn = !speakerOn;


        try {
            call.setSpeakerMode(speakerOn);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacks(mUpdateTimeTask);

        if (call != null){
            call.close();

        }

    }


}
