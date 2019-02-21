package com.hfad.freef;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 *Aktynowść ta odpowiedzialna jest za udostepnianie listy
 * aktywnych uzytkownikow. Lista ma postac odswierzalnej listy,
 *, ktora odswiezana jest po smagnieciu palcem. W przypadku wybrania
 * konkretnego elementu listy nastepuje realizacja polaczenia
 * i za pomoca intencji uruchamiana jest aktywnosc CallActivity_new.
 */

public class DisplayUsersActivity extends AppCompatActivity {
    /**
     Pozwolenie na odczytywanie stanu urzadzenia.
     */
    public final static int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 11;
    /**
     lista uzytkownikow, ktora ma byc wyswietlana na liscie
     aktywnych uzytkownikow.
     */
    public static List<String> users = new ArrayList<>();
    /**
     adapter
     */
    public static ArrayAdapter<String> listAdapter;
    /**
     odswiezalna lista
     */
    public static SwipeRefreshLayout pullToRefresh;
    /**
     widok listy
     */
    public static ListView listView;
    /**
     Obiekt ten jest BroadcastReceiverem, ktory wyzwalany jest
     w momencie wykrycia przychodzacego polaczenia.
     */
    public IncomingCallService_new callReceiver;
    /**
     Button do zmiany statusu uzytkownika.
     Online/Busy.
     */
    public static ToggleButton changePresenceStatus;

    /**
     Element menu, pozwalajacy na edycje danych uzytkownika.
     */
    private static final int UPDATE_PROFILE_DATA = 1;
    private TextView statusText;

    /**
     nazwa uzytkownika
     */
    public String USERNAME = null;
    /**
     haslo uzytkownika
     */
    public String PASSWORD = null;
    /**
     adres ip serwera
     */
    public static String SERVER = null;
    /**
     Manager Sip niezbedny do implementacji
     VoIP
     */
    public static SipManager manager;
    /**
     Profil uzytkwonika
     */
    public static SipProfile localProfile;

    static  public boolean sipRegisterDone = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_users);
//        startService(new Intent(this,DetectConnectionService.class));



        /**
         Obiekt pozwalajcy na odswiezanie listy aktywnych uzytkownikow
         */
        final  ActiveUsers ac =   new ActiveUsers();


        // sprawdzenie pozwolenia
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        }
        // pobranie informacji o wczesniejszej rejestracji SIP
        if (savedInstanceState != null) {

            sipRegisterDone = savedInstanceState.getBoolean("wasRegistered");
        }

        pullToRefresh = (SwipeRefreshLayout) findViewById(R.id.pullToRefresh);
        changePresenceStatus = (ToggleButton) findViewById(R.id.changePresence);

        // filtr intencji, ktory reaguje na przychodzace polaczenia
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.SipDemo.INCOMING_CALL");


        callReceiver = new IncomingCallService_new();
        // sparowanie filtru z BroadcastReceiverem oczekujacym na polaczenia przychodzace
        this.registerReceiver(callReceiver, filter);

        final ListView listView = (ListView) findViewById(R.id.mobile_lis);
        listView.setAdapter(listAdapter);

        changePresenceStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPresenceStatusChanged(v);

            }
        });

        // odswiezanie listy w wątku
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                Thread th = new Thread(ac);
                th.start();
                try {

                    th.join();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter = new ArrayAdapter<String>(DisplayUsersActivity.this, R.layout.activity_listview, users);
                        listView.setAdapter(listAdapter);
                        listAdapter.setNotifyOnChange(true);
                        listAdapter.notifyDataSetChanged();
                        pullToRefresh.setRefreshing(false);

                    }


                });

            }
        });



        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> listView,
                                    View itemView,
                                    int position,
                                    long id) {

                String users = ((TextView)itemView).getText().toString();

                Intent i = new Intent(getApplicationContext(),CallActivity_new.class);
                //intencja uzupelniana jest o informacje o uzytkowniku docelowym.
                // intecja ta uruchamia aktynowsc CallActivity, gdzie realizowane jest
                // to polaczenie wychodzace
                i.putExtra("SIP_NAME_TO_CALL", users);
                try {
                    if(manager.isOpened(localProfile.getUriString()))
                        startActivity(i);
                } catch (SipException e) {
                    e.printStackTrace();
                }


            }
        };

        listView.setOnItemClickListener(itemClickListener);

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                }
                return;
            }
        }
    }
    /**
     metoda ta przechowuje informacje o wczesniejszym zarejestrowaniu SIP
     @param savedInstanceState - obiekt pozwalajcy na przechowanie informacji.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putBoolean("wasRegistered",sipRegisterDone);
    }

    /**
     *Metoda obslugujaca zmiane statusu dostepnosci uzytkownika.
     * Metoda umozliwia wyrejestrowanie nas z serwera i zamkniecie sesji.

     @param view  - toggleButton zmieniajacy stan dostepnosci uzytkownika
     */
    public void onPresenceStatusChanged(View view) {

        // Pobieramy stan przycisku toggleButton
        boolean on = ((ToggleButton) view).isChecked();
        // jesli przycisk w stanie 'busy'
        //wyrejestruj nas z serwera
        if (!on) {

            FlushUser flushUser = new FlushUser();
            Thread fu = new Thread(flushUser);

            fu.start();
            try {
                fu.join();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            closeLocalProfile();


        } else {
            sipRegisterDone = false;

            if(sipRegisterDone==false) {
                initializeManager();

            }
        }
    }

    /**
     *inicjalizacja sipManagera przy uruchomieniu widoku glownego aplikacji
     */
    @Override
    protected void onStart() {
        super.onStart();
        if(sipRegisterDone==false) {
            initializeManager();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FlushUser flushUser = new FlushUser();
        Thread fu = new Thread(flushUser);

        fu.start();
        try {

            fu.join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sipRegisterDone = false;


        if(callReceiver!=null)
        {

            this.unregisterReceiver(callReceiver);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
//        closeLocalProfile();
    }

    /**
     *Metoda tworzaca menu aplikacji.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, UPDATE_PROFILE_DATA, 0, "Edytuj dane do konta SIP");

        return true;
    }
    /**
     * obsluga wybrania konkretnego elementu menu
     * @param  item - element menu
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case UPDATE_PROFILE_DATA:
                updateLocalProfile();
                break;
        }
        return true;
    }
    /**
     * Aktualizacja profilu SIP.
     */
    public void updateLocalProfile() {
        Intent profileUpdate = new Intent(getBaseContext(), SipSettings.class);
        startActivity(profileUpdate);
    }

    public void updateStatus (final String status) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                statusText.setText(status);
            }
        });
    }
    /**
     *inicjalizacja SipManagera, niezbednego w implementacji VoIP.
     * Odpowiedzialny za rejestrowanie profilu Sip na serwerze,
     * jego wyrejestrowanie, udostepnianie informacji o uzytkownikach itd.
     */
    public void initializeManager() {
        if (manager == null) {
            manager = SipManager.newInstance(this);
//            isSipRegisterDone = true;
        }
        initializeLocalProfile();
    }

    /**
     *inicjalizacja profilu Sip, glownie informacji o nazwie uzytkownika
     * hasle oraz adresie serwera.
     */
    public void initializeLocalProfile() {
        if (manager == null) {
            return;
        }
        if (localProfile != null) {
            closeLocalProfile();
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        USERNAME = preferences.getString("username", "");
        PASSWORD = preferences.getString("password", "");
        SERVER = preferences.getString("server", "");


        if (USERNAME.length() == 0 || PASSWORD.length() == 0) {
            updateStatus("WprowadĹş dane");
            return;
        }

        try {
            SipProfile.Builder profileBuilder = new SipProfile.Builder(USERNAME, SERVER);
            profileBuilder.setPassword(PASSWORD);
            profileBuilder.setAutoRegistration(true);
            profileBuilder.setPort(5060);
            profileBuilder.setProtocol("UDP");
            profileBuilder.setOutboundProxy(SERVER);
            localProfile = profileBuilder.build();

            Intent intent = new Intent();
            intent.setAction("android.SipDemo.INCOMING_CALL");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, Intent.FILL_IN_DATA);
            manager.open(localProfile, pendingIntent, null);

            /**
             *Rejestracja uzytkownika w serwerze z wykorzystaniem listenera rejestracji SIP.
             */
            manager.setRegistrationListener(localProfile.getUriString(), new SipRegistrationListener() {
                @Override
                public void onRegistering(String s) {
                    updateStatus("Rejestrowanie do SIP Serwera");

                }

                @Override
                public void onRegistrationDone(String s, long l) {
                    updateStatus("Gotowy");
                    sipRegisterDone = true;


                }

                @Override
                public void onRegistrationFailed(String s, int i, String s1) {
                    updateStatus("SprawdĹş ustawienia. BĹ‚Ä…d rejestracji");

                }
            });


        } catch (ParseException pe) {
            updateStatus("BĹ‚Ä…d");
        } catch (SipException se) {
            updateStatus("BĹ‚Ä…d");
        }
    }
    /**
     *zamykanie profilu SIP - zamykanie sesji
     */
    public void closeLocalProfile() {
        if (manager == null){
            return;
        }
        try {
            if (localProfile != null) {

                manager.close(localProfile.getUriString());

                Log.d("localProfile: .", localProfile.getUriString());
            }
        } catch (Exception e) {
            Log.d("Profile Close Error", "BĹ‚Ä…d zamkniÄ™cia profilu", e);
        }
    }

}




