package com.hfad.freef;

import org.freeswitch.esl.client.inbound.Client;
import org.freeswitch.esl.client.inbound.InboundConnectionFailure;
import org.freeswitch.esl.client.transport.message.EslMessage;

;
/**
 * Klasa odpowiedzialna za pobranie informacji o
 * aktywnych uzytkownikach z serwera Freeswitch.
 * Proces pobierania tych informacji jest wykonywany w
 * watku (operacja sieciowa).
 */
public class ActiveUsers implements Runnable  {

    /**
     * obiekt klasy RegexMatches, ktory finalnie przechowuje
     * informacje o aktywnych uzytkownikach
     */
    RegexMatches regexMatches = new RegexMatches();

    /**
     * obiekt klasy DisplayUSersActivity, ktory umozliwia
     * odwolanie sie od obiektu users tej klasy i uzupelnienie
     * go o aktualnych uzytkwonikow
     */
    DisplayUsersActivity dp = new DisplayUsersActivity();


    @Override
    public void run() {
        /**
         * obiekt klasy Client, pochodzacej z
         * biblioteki freeswitch
         */
        Client client = new Client();
        //laczymy sie z Freeswitch na sockecie, ktory umozlwia komunikacje z FS
        try {
            client.connect(DisplayUsersActivity.SERVER, 8021, "ClueCon", 1);
            //zwroc zalogowanych Sip
            EslMessage resp =  client.sendSyncApiCommand("sofia", "status profile internal reg");


            if(resp.getBodyLines()!= null){
                regexMatches.INPUT = resp.getBodyLines().toString();
                DisplayUsersActivity.users = regexMatches.getAll(regexMatches.INPUT);
            }

            client.close();

        } catch (InboundConnectionFailure inboundConnectionFailure) {
            inboundConnectionFailure.printStackTrace();
        }


    }
}