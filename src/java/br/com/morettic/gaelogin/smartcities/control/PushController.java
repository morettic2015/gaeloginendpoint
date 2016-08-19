/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

import br.com.morettic.gaelogin.smartcities.vo.DeviceType;
import br.com.morettic.gaelogin.smartcities.vo.Perfil;
import br.com.morettic.gaelogin.smartcities.vo.PushDevice;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import java.util.List;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author LuisAugusto
 */
public class PushController {

    private static PersistenceManager pm = null;

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
        js.put("devices", ja);
        return js;
    }
}
