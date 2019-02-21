package com.hfad.freef;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 *Klasa ta przechowuje obiekty posiadajace informacje o profilach SIP.
 * Glownie: nazwa, haslo, adres serwera.
 */
public class SipSettings extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.local_profile);
    }
}

