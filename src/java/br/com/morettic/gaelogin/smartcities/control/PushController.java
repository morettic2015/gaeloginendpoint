/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

import br.com.morettic.gaelogin.smartcities.vo.DeviceType;
import br.com.morettic.gaelogin.smartcities.vo.LogLocalizacao;
import br.com.morettic.gaelogin.smartcities.vo.Perfil;
import br.com.morettic.gaelogin.smartcities.vo.PushDevice;
import br.com.morettic.gaelogin.smartcities.vo.Registro;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static java.net.URLEncoder.encode;

/**
 *
 * @author LuisAugusto
 */
public class PushController {

    private static PersistenceManager pm = null;
    private static final Double UMK = 0.1570d;

    public static Double calcLat(int distance) {
        return (Double) (distance * UMK / 1000);
    }

    public static final JSONObject registerUserDevice(HttpServletRequest req, HttpServletResponse res) throws JSONException {
        JSONObject js = new JSONObject();

        pm = PMF.get().getPersistenceManager();

        Long idUser = Long.parseLong(req.getParameter("idUser"));
        String token = req.getParameter("token");
        String so = req.getParameter("so");

        PushDevice myDevice = null;
        try {
            myDevice = (PushDevice) pm.getObjectById(idUser);
        } catch (Exception e) {
            myDevice = new PushDevice(idUser, token, DeviceType.valueOf(so));
        } finally {
            myDevice.setDeviceToken(token);
            myDevice.setSo(DeviceType.valueOf(so));

            pm.makePersistent(myDevice);
            pm.close();

            js.put("id", myDevice.getKey());
            js.put("token", myDevice.getDeviceToken());
            js.put("so", myDevice.getSo().toString());
            js.put("user", myDevice.getIdProfile());
///////
            return js;
        }
    }

    public static final JSONObject getRegisteredDevices(HttpServletRequest request) throws JSONException {
        JSONObject js = new JSONObject();

        pm = PMF.get().getPersistenceManager();
        Query devices = pm.newQuery(PushDevice.class);
        String main = "% :" + request.getParameter("msg");
        List<PushDevice> lPushes = (List<PushDevice>) devices.execute();
        JSONArray ja = new JSONArray();
        for (PushDevice p : lPushes) {
            try {
                JSONObject js1 = new JSONObject();

                Perfil p1 = pm.getObjectById(Perfil.class, p.getIdProfile());

                js1.put("token", p.getDeviceToken());
                String msgFinal = main.replace("%", p1.getNome());
                js1.put("msg", msgFinal);
                ja.put(js1);
            } catch (Exception exception) {
                exception.printStackTrace();
                continue;
            }
        }
        pm.close();
        js.put("devices", ja);
        return js;
    }

    public static JSONObject sendPushResumeFromLocation(HttpServletRequest request) throws JSONException {
        JSONObject js = new JSONObject();
        pm = PMF.get().getPersistenceManager();
        String token = request.getParameter("token");
        //Save last user position....TRACKER
        //Locale lat lon
        double lon = Double.parseDouble(request.getParameter("lon"));
        double lat = Double.parseDouble(request.getParameter("lat"));
        //Get user id based on device token
        //try to persist
        try {
            String id = request.getParameter("id");
            String ipAddress = request.getHeader("X-FORWARDED-FOR") != null ? request.getHeader("X-FORWARDED-FOR") : request.getRemoteAddr();
            LogLocalizacao logLocale = new LogLocalizacao(token, new Long(id), lat, lon, ipAddress);
            pm.makePersistent(logLocale);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Filter count to send push
        String filter = "this.latitude>=latMin && this.latitude<=latMax ";
        Query q = pm.newQuery(Registro.class, filter);
        q.declareParameters("Float latMin,Float latMax");
        Set<Registro> lSOcorrencias = new HashSet<Registro>();

        double latMax, latMin, q1;

        //Adiciona todos
        int total = 0;

        q1 = calcLat(5);
        latMax = (lat + q1);
        latMin = (lat - q1);

        js.put("latMax", latMax);
        js.put("latMin", latMin);

        double lonMax = lon + (5 * 0.0009);
        double lonMin = lon - (5 * 0.0009);

        js.put("lonMax", lonMax);
        js.put("lonMin", lonMin);

        lSOcorrencias.addAll((List<Registro>) q.execute(latMin, latMax));
        for (Registro o : lSOcorrencias) {
            double mLon = new Double(o.getLongitude());
            if (!(mLon >= lonMin) && (mLon <= lonMax)) {
                continue;
            }
            total++;
        }

        String msg = total + "_news_around_you!";
        //I
        if (total < 1) {
            msg = "Share_something_with_us!Be_the_first!";
        }

        String url = HTTPWWWUNIVOXERCOM8080PUSH_IOSINGLE_PUSHI + token + "&msg=" + msg;
        js.put("push", URLReader.readUrl(url));
        js.put("url", url);
        js.put("msg", msg);

        pm.close();
        return js;
    }
    public static final String HTTPWWWUNIVOXERCOM8080PUSH_IOSINGLE_PUSHI = "http://www.univoxer.com:8080/push_io/single_push.io?token=";
}
