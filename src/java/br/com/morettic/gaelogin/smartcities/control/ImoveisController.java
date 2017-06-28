/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

import static br.com.morettic.gaelogin.smartcities.control.PerfilController.getOpenStreeMapCollection;
import static br.com.morettic.gaelogin.smartcities.control.URLReader.readJSONUrl;
import br.com.morettic.gaelogin.smartcities.vo.TipoOcorrencia;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.Normalizer;

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

    public static final JSONObject getLocationsFromNeighohood(String lat, String lon) throws JSONException {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lon + "&sensor=true&key=AIzaSyCkJEjT73RmsOw1Ldy3S9RbWg_-PDRh8zE";
        JSONObject address = readJSONUrl(url);
        String city = null;
        JSONObject js = new JSONObject();
        js.put("address", address);
        js.put("url", url);

        JSONObject treee = address.getJSONArray("results").getJSONObject(0);
        JSONArray ja = treee.getJSONArray("address_components");
        for (int i = 0; i < ja.length(); i++) {
            if (!ja.getJSONObject(i).getJSONArray("types").getString(0).equals("administrative_area_level_2")) {
                continue;
            }
            city = ja.getJSONObject(i).getString("long_name");
            js.put("c0", city);
            city = Normalizer.normalize(city, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        }
        //City is null no administrative_area_level_2 found to search
        if (city == null) {
            return js;
        }
        js.put("c1", city);
        JSONArray jOpenStreetMap = new JSONArray();
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "RODOVIARIA", TipoOcorrencia.INFRAESTRUTURA.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "POSTO", TipoOcorrencia.INFRAESTRUTURA.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "BANCO", TipoOcorrencia.INFRAESTRUTURA.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "FUNDACAO", TipoOcorrencia.INFRAESTRUTURA.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "SAUDE", TipoOcorrencia.SAUDE.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "UPA", TipoOcorrencia.SAUDE.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "FARMACIA", TipoOcorrencia.SAUDE.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "TEATRO", TipoOcorrencia.CULTURA.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "EVENTOS", TipoOcorrencia.CULTURA.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "CINEMA", TipoOcorrencia.CULTURA.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "ESPORTE", TipoOcorrencia.ESPORTE.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "FUTEBOL", TipoOcorrencia.ESPORTE.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "TENIS", TipoOcorrencia.ESPORTE.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "SURF", TipoOcorrencia.ESPORTE.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "SKATE", TipoOcorrencia.ESPORTE.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "GOLF", TipoOcorrencia.ESPORTE.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "TAXI", TipoOcorrencia.TRANSPORTE.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "TERMINAL", TipoOcorrencia.TRANSPORTE.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "PONTO DE ONIBUS", TipoOcorrencia.TRANSPORTE.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "RODOVIARIA", TipoOcorrencia.TRANSPORTE.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "EDUCACAO", TipoOcorrencia.EDUCACAO.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "UNIVERSIDADE", TipoOcorrencia.EDUCACAO.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "FACULDADE", TipoOcorrencia.EDUCACAO.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "ESCOLA", TipoOcorrencia.EDUCACAO.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "CURSO", TipoOcorrencia.EDUCACAO.toString()));
        jOpenStreetMap.put(getOpenStreeMapCollection(city, "BIBLIOTECA", TipoOcorrencia.EDUCACAO.toString()));

        JSONArray openStreetFinal = new JSONArray();
        for (int i = 0; i < jOpenStreetMap.length(); i++) {
            ja = jOpenStreetMap.getJSONArray(i);
            for (int x = 0; x < ja.length(); x++) {
                openStreetFinal.put(ja.getJSONObject(x));
            }

        }

        js.put("results", openStreetFinal);
        return js;

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
