package com.cms.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MailUtil {

    private static final String BASE_URL = "https://script.google.com/macros/s/AKfycbzjqjbtaAzXkAejx5265ow8IbhaOttcyrY7bdGfGzmzwZEpQfoZoIu5LpKQSuTrPA-caQ/exec";

    public static boolean inviaMail(String msg, String dest, String subject) {
        try {
            String encodedDest = URLEncoder.encode(dest, "UTF-8");
            String encodedMsg = URLEncoder.encode(msg, "UTF-8");
            String encodedSubject = URLEncoder.encode(subject, "UTF-8");

            String fullUrl = BASE_URL + "?to=" + encodedDest + "&subject=" + encodedSubject + "&body=" + encodedMsg;

            URL url = new URL(fullUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Ricezione risposta
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
