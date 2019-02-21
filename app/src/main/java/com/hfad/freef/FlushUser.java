package com.hfad.freef;

import org.freeswitch.esl.client.inbound.Client;
import org.freeswitch.esl.client.inbound.InboundConnectionFailure;
import org.freeswitch.esl.client.transport.message.EslMessage;

/**
 *Klasa odpowiedzialna za wyrejestrowanie danego sipProfila z serwera
 */
public class FlushUser implements  Runnable{


    @Override
    public void run() {

        Client client = new Client();
        //laczymy sie z Freeswitch na sockecie, ktory umozlwia komunikacje z FS
        try {
            client.connect(DisplayUsersActivity.SERVER, 8021, "ClueCon", 1);
            /**
             *uzytkownik, ktorego chcemy wyrejestrowac
             */
            String user = DisplayUsersActivity.localProfile.getUserName();
            // komenda odpowiedzialna za wyrejestrowanie uzytkownikow
            String command = "profile internal flush_inbound_reg "+user+"@"+DisplayUsersActivity.SERVER;
            /**
             *zwrocona odpowiedz
             */
            EslMessage resp =  client.sendSyncApiCommand("sofia", command);

            client.close();

        } catch (InboundConnectionFailure inboundConnectionFailure) {
            inboundConnectionFailure.printStackTrace();
        }

    }

}
