/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import javax.jdo.PersistenceManager;

/**
 * Query q = pm.newQuery("select from
 * br.com.morettic.gaelogin.smartcities.vo.Registro where tipo == 'MANGUE_VIVO'
 * order by dtOcorrencia desc"); List<Registro> denuncias = (List<Registro>)
 * q.execute();
 *
 * @author LuisAugusto
 */
public class ManguevivoController implements Search {

    private static PersistenceManager pm = null;

    @Override
    public JSONArray doSearch() throws JSONException {
        pm = PMF.get().getPersistenceManager();

        /* String filter = "this.latitude>=latMin && this.latitude<=latMax ";
         Query q = pm.newQuery(Registro.class, filter);
         q.declareParameters("Float latMin,Float latMax");
         double lat1 = -27d;
         double latMax, latMin, q1;
         q1 = calcLat(2000);
         latMax = (lat1 + q1);
         latMin = (lat1 - q1);*/
        JSONArray ja = new JSONArray();
        // String tp = TipoOcorrencia.MANGUE_VIVO.toString();
      /*  List<Registro> denuncias = (List<Registro>) q.execute(latMin, latMax);

         for (Registro denuncia : denuncias) {

         if (!denuncia.getTipo().equals(TipoOcorrencia.MANGUE_VIVO)) {
         continue;
         }

         Perfil p = pm.getObjectById(Perfil.class, denuncia.getPerfil());

         JSONObject js = new JSONObject();
         js.put("id", denuncia.getKey());
         js.put("lat", denuncia.getLatitude());
         js.put("lon", denuncia.getLongitude());
         js.put("tit", denuncia.getTipo());
         js.put("desc", denuncia.getDescricao());
         js.put("author", p.getNome());
         js.put("email", p.getEmail());
         js.put("token", getUrlFromImage(pm, denuncia.getAvatar()));
         js.put("avatar", getUrlFromImage(pm, p.getAvatar()));

         ja.put(js);
         }
         denuncias.clear();
         pm.close();*/

        return ja;

//To change body of generated methods, choose Tools | Templates.
    }

}
