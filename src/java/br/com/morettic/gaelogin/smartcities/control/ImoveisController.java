/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *
 * @author LuisAugusto
 */
public class ImoveisController implements Search {

    private JSONArray ja;
    private Integer distance;
    private Double latitude, longitude;

    public ImoveisController(Double lat, Double lon, Integer d) {
        this.distance = d;
        this.latitude = lat;
        this.longitude = lon;
    }

    public String getUrl() {
        distance = distance < 1000 ? 1000 : distance;//Distancia minima 1 km
        String url = "http://www.genimo.com.br/api/fcitywatch/news/" + latitude + "/" + longitude + "/" + distance;
        return url;
    }

    public JSONArray doSearch() throws JSONException {
        //try {
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(getUrl());

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            ja = new JSONArray(sb.toString());
        } catch (Exception e) {
            ja = new JSONArray();
            e.printStackTrace();
        } finally {
            return ja;
        }
    }
}
