package com.hfad.freef;



import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Klasa, ktora parsuje zwrocne wyniki z serwera Freeswitch.
 * Obiekt tej klasy wykorzystywany jest w klasie ActiveUsers.
 */
public class RegexMatches {
    public  static String INPUT = null;

    /**
     *wzor, ktory jest pozadany
     */
    private final static String REGEX = "sip:\\S+\\@";
    /**
     *lista aktywnych uzytkownikow
     */
    public static List<String> getAll(String input){
        List<String> list = new ArrayList<>();
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(input);


        while(matcher.find()){

            list.add(matcher.group().replace("@", "").replace("sip:", ""));

        }
        // zwrocenie listy wszystkich aktywnych kontaktow
        return list;
    }
}