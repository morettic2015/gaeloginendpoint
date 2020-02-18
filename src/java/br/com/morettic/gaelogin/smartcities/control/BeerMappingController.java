/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

import br.com.morettic.gaelogin.smartcities.vo.TipoOcorrencia;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import java.util.Date;

/**
 * &lat=-27.581689721999503&lng=-48.67871541684572&
 *
 * @author LuisAugusto
 */
public class BeerMappingController implements Search {

    public static final String BEER_MAPPING_URL = "https://beermapping.com/includes/dataradius.php?radius=100.359648824126898";

    private double lat, lon;

    public BeerMappingController(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public JSONArray doSearch() throws JSONException {
        StringBuilder sb = new StringBuilder(BEER_MAPPING_URL);
        sb.append("&lat=");
        sb.append(lat);
        sb.append("&lng=");
        sb.append(lon);

        JSONObject js = URLReader.readJSONUrl(sb.toString());

        JSONArray ja1 = js.getJSONArray("locations");
        JSONArray ret = new JSONArray();

        for (int i = 0; i < ja1.length(); i++) {
            JSONObject js1 = new JSONObject();
            js1.put("id", ja1.getJSONObject(i).getString("id"));
            js1.put("lon", ja1.getJSONObject(i).getString("lng"));
            js1.put("desc", ja1.getJSONObject(i).getString("status") + ' ' + ja1.getJSONObject(i).getString("phone"));
            //js1.put("token3", null);

            js1.put("token", "https://ipagrow.com/images/logo.png");
            //js1.put("token", null);
            js1.put("address", ja1.getJSONObject(i).getString("street") + '-' + ja1.getJSONObject(i).getString("city"));
            //js1.put("token1", null);
            //js1.put("token2", null);
            js1.put("tipo", TipoOcorrencia.BEER.toString());
            js1.put("tit", ja1.getJSONObject(i).getString("name"));
            js1.put("rating", ja1.getJSONObject(i).getString("total"));
            js1.put("date", new Date());
            js1.put("lat", ja1.getJSONObject(i).getString("lat"));
            ret.put(js1);
        }
        ja1 = null;
        js = null;
        sb = null;
        return ret;
    }

}
