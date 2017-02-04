/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import static java.net.URLEncoder.encode;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author LuisAugusto
 */
public class URLReader {

    public static final String readUrl(String murl) {
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(murl);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            return sb.toString();
        }
    }

    public static final JSONArray readJSONArrayUrl(String murl) throws JSONException {
        StringBuilder sb = null;
        try {
            URL url = new URL(murl);
            InputStreamReader isr = new InputStreamReader(url.openStream(), "UTF-8");
            //String encoding = isr.getEncoding(); //if you actually need it, which I don't suppose
            BufferedReader in = new BufferedReader(isr);
            String readLine;
            sb = new StringBuilder();
            while ((readLine = in.readLine()) != null) {
                sb.append(readLine);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return new JSONArray(sb.toString());
        }
    }

    public static final JSONObject readJSONUrl(String murl) {
        JSONObject elemento = new JSONObject();
        try {
            URL url = new URL(murl);
            StringBuilder sb;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
                String line;
                sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            elemento = new JSONObject(sb.toString());

        } catch (IOException | JSONException e) {
            elemento = new JSONObject();
            elemento.put("ERROR", e.toString());
            e.printStackTrace();
        } finally {
            return elemento;
        }
    }

    private static final String[] HEADERS_TO_TRY = {
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_X_FORWARDED_FOR",
        "HTTP_X_FORWARDED",
        "HTTP_X_CLUSTER_CLIENT_IP",
        "HTTP_CLIENT_IP",
        "HTTP_FORWARDED_FOR",
        "HTTP_FORWARDED",
        "HTTP_VIA",
        "REMOTE_ADDR"};

    public static final String getClientIpAddress(HttpServletRequest request) {
        for (String header : HEADERS_TO_TRY) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * @get Genimo webservice data!
     *
     *
     * [
     * {
     * idProperty: "213", vlRental: "2000.00", vlSale: null, vlSeasonRent: null,
     * vlLatitude: "-26.99902300000000000000", vlLongitude:
     * "-48.62688320000000000000", nmProperty: null, dsProperty: null,
     * nmCategory: "Apartamento", nmPersonBookie: null, nuCelPhoneBookie: null,
     * dsEmailBookie: null, nmCompany: "Cidades Imobiliária - BC", dsAddress:
     * "Rua 3.100, nº 270", dsCompanyLogo:
     * "HTTP://www.genimo.com.br/logos/32.jpg", nmPicture: null, vlDistance:
     * "0.157535365702884" },
     *
     */
    public static final String getUrlOpenStreetMap(String city, String service) {
        StringBuilder sb = new StringBuilder("http://nominatim.openstreetmap.org/search?q=");
        sb.append(URLEncoder.encode(city));
        sb.append(URLEncoder.encode(" "));
        sb.append(URLEncoder.encode(service));
        sb.append("&format=json&polygon=0&addressdetails=1&accept-language=en-US,pt-BR");
        return sb.toString();
    }

    public static final JSONArray getWebhoseIoResults(String aq) throws JSONException {
        StringBuilder sb = new StringBuilder("http://webhose.io/search?token=1fc216b6-77ed-4ab8-8afb-a3ba12a44e09&format=json&ts=1485912032309q=");
        sb.append(URLEncoder.encode(aq));
        JSONObject js = new JSONObject(readJSONUrl(sb.toString()));
        //Retorno
        JSONArray ja = new JSONArray();
        JSONArray jaFull = js.getJSONArray("posts");
        /**
         * id: "e5dff2f5ec0154fcd6d3e0408212c3eac68b1684", 
         * author: "Agencia
         * RBS", 
         * title: "UFSC de luto: morre o professor João Benjamin Cruz",
         * text: "Moacir Pereira 03/01/2017 | 21h05 Atualizada em 03/01/2017 |
         * 21h10 UFSC de luto: morre o professor João Benjamin Cruz O professor
         * destacou-", 
         * lon: -51.2,
         * token:
         * "http://anoticia.rbsdirect.com.br/imagesrc/22107036.jpg?w=200",
         * idWebPage: 1,
         * date: "2017-01-04T06:05:00.000+02:00", 
         * lat: -30.0333,
         * country: "Brazil", 
         * city: "Porto Alegre"
         *
         */
        for (int i = 0; i < jaFull.length(); i++) {
            JSONObject localNew = new JSONObject();
            localNew.put("id", jaFull.getJSONObject(i).getString("uuid"));
            localNew.put("title", jaFull.getJSONObject(i).getString("title"));
            localNew.put("text", jaFull.getJSONObject(i).getString("text"));
            localNew.put("lon", "");
            localNew.put("token", jaFull.getJSONObject(i).getJSONObject("thread").getString("main_image"));
            localNew.put("idWebPage", "");
            localNew.put("date", jaFull.getJSONObject(i).getJSONObject("thread").getString("published"));
            localNew.put("lat", "");
            localNew.put("country", "");
            localNew.put("city", "");
            ja.put(localNew);
            
        }

        return ja;
    }

    public static final String getWebhoseIo(String aq) {
        return "http://citywatch.com.br/v1/webhoseNews.php?query=" + URLEncoder.encode(aq);
    }

    public static final String getTwitter(String aq) {
        return "http://citywatch.com.br/v1/twitterNews.php?query=" + URLEncoder.encode(aq);
    }
}
