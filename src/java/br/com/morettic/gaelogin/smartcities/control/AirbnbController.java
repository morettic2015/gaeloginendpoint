/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

import br.com.morettic.gaelogin.smartcities.vo.TipoOcorrencia;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import java.net.URLEncoder;
import java.util.Date;

/**
 *
 * @author LuisAugusto
 */
public class AirbnbController implements Search {

    private JSONArray airBnb;
    private StringBuilder sbUrl = new StringBuilder();

    public AirbnbController(String location, double lat, double lon) {
        sbUrl.append("https://api.airbnb.com/v2/search_results?client_id=3092nxybyb0otqw18e8nh5nty&locale=pt-BR&currency=BRL&_format=for_search_results_with_minimal_pricing&_limit=20&_offset=0&fetch_facets=true&guests=1&ib=true&ib_add_photo_flow=true&location=");
        sbUrl.append(URLEncoder.encode(location));
        sbUrl.append("&min_bathrooms=0&min_bedrooms=0&min_beds=1&min_num_pic_urls=10&price_max=210&price_min=40&sort=1&user_lat=");
        sbUrl.append(lat);
        sbUrl.append("&user_lng=");
        sbUrl.append(lon);
        this.airBnb = new JSONArray();

    }

    @Override
    public JSONArray doSearch() throws JSONException {

        JSONObject full = URLReader.readJSONUrl(sbUrl.toString());
        JSONArray jaTmp = full.getJSONArray("search_results");
        for (int i = 0; i < jaTmp.length(); i++) {

            JSONObject listing = new JSONObject();

            JSONObject l1 = jaTmp.getJSONObject(i).getJSONObject("listing");

            listing.put("id", l1.getInt("id"));
            listing.put("lon", l1.getString("lng"));
            listing.put("desc", l1.getString("room_type")
                    + ' '
                    + l1.getString("name")
                    + " Bathroom:"
                    + l1.getString("bathrooms")
                    + " Bedroom:"
                    + l1.getString("bedrooms")
                    + " beds:"
                    + l1.getString("beds")
                    + " person_capacity:"
                    + l1.getString("person_capacity")
            );
            listing.put("token", l1.getString("xl_picture_url"));
            listing.put("gallery", l1.getJSONArray("picture_urls"));
            listing.put("host", l1.getJSONObject("user").getString("first_name"));
            listing.put("host_avatar", l1.getJSONObject("user").getString("picture_url"));
            //js1.put("token", null);
            listing.put("address", l1.getString("public_address"));
            //js1.put("token1", null);
            //js1.put("token2", null);
            listing.put("tipo", TipoOcorrencia.AIRBNB.toString());
            listing.put("tit", l1.getString("name"));
            listing.put("rating", "0.0");
            listing.put("date", new Date());
            listing.put("lat", l1.getString("lat"));
            airBnb.put(listing);
        }

        return this.airBnb;
    }

}
