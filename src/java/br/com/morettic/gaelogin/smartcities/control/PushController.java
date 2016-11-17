/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

import br.com.morettic.gaelogin.smartcities.vo.Chat;
import br.com.morettic.gaelogin.smartcities.vo.Contato;
import br.com.morettic.gaelogin.smartcities.vo.DeviceType;
import br.com.morettic.gaelogin.smartcities.vo.LogLocalizacao;
import br.com.morettic.gaelogin.smartcities.vo.Perfil;
import br.com.morettic.gaelogin.smartcities.vo.Promocao;
import br.com.morettic.gaelogin.smartcities.vo.PushDevice;
import br.com.morettic.gaelogin.smartcities.vo.Registro;
import br.com.morettic.gaelogin.smartcities.vo.TipoOcorrencia;
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
import java.util.ArrayList;
import java.util.Collections;

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
                continue;
            }
        }
        pm.close();
        js.put("devices", ja);
        return js;
    }

    /**
     * @param request String tit, String msg, Long from, List<TipoOcorrencia>
     * canais
     * @return JSONObject
     * @throws com.google.appengine.labs.repackaged.org.json.JSONException
     * @Send message to the destiny
     */
    public static final JSONObject sendPromo(HttpServletRequest request) throws JSONException {
        JSONObject js = new JSONObject();
        pm = PMF.get().getPersistenceManager();

        List<TipoOcorrencia> canais = new ArrayList<>();
        String tipos[] = request.getParameter("anais").split(",");
        for (String tp1 : tipos) {
            canais.add(TipoOcorrencia.valueOf(tp1));
        }

        //Create chat message into Datastore
        Promocao promo = new Promocao(request.getParameter("tit"), request.getParameter("msg"), Long.parseLong(request.getParameter("from")), canais);

        js.put("promoId", promo.getKey());
        JSONArray js1 = new JSONArray();
        //Locate PushDevice from destiny message
        Query qPush = pm.newQuery(PushDevice.class);
        List<PushDevice> lRet = (List<PushDevice>) qPush.execute();
        List<Long> lIds = new ArrayList<>();
        for (PushDevice pd : lRet) {
            js1.put(pd.getIdProfile());
            String url = HTTPWWWUNIVOXERCOM8080PUSH_IOSINGLE_PUSHI + pd.getDeviceToken() + "&msg=" + promo.getTit();
            js.put("push", URLReader.readUrl(url));
            js.put("url", url);
            js.put("hash", pd.getDeviceToken());
            lIds.add(pd.getIdProfile());
        }

        //Save promotion
        promo.setlDestinatarios(lIds);
        pm.makePersistent(promo);

        js.put("sentTo", js1);
        pm.close();
        return js;
    }

    /**
     * @param request Chat chatMessage = new Chat(request.getParameter("tit"),
     * request.getParameter("msg"), request.getParameter("from"),
     * request.getParameter("to"));
     * @return JSONObject
     * @throws com.google.appengine.labs.repackaged.org.json.JSONException
     * @Send message to the destiny
     */
    public static final JSONObject sendMessage(HttpServletRequest request) throws JSONException {
        JSONObject js = new JSONObject();
        pm = PMF.get().getPersistenceManager();

        //Create chat message into Datastore
        Chat chatMessage = new Chat(request.getParameter("tit"), request.getParameter("msg"), request.getParameter("from"), request.getParameter("to"));
        pm.makePersistent(chatMessage);
        js.put("chatId", chatMessage.getKey());

        //Locate PushDevice from destiny message
        Query qPush = pm.newQuery(PushDevice.class);
        String pQuery = "idProfile == :idProfile";
        qPush.setFilter(pQuery);
        Long to = Long.parseLong(request.getParameter("to"));
        List<PushDevice> lRet = (List<PushDevice>) qPush.execute(to);

        //If registered send push notification to the user about the new message!
        if (lRet.size() > 0) {
            PushDevice push = lRet.get(0);
            String url = HTTPWWWUNIVOXERCOM8080PUSH_IOSINGLE_PUSHI + push.getDeviceToken() + "&msg=Nova_Mensagem!";
            js.put("push", URLReader.readUrl(url));
            js.put("url", url);
            js.put("hash", push.getDeviceToken());
        }
        pm.close();
        return js;
    }

    /**
     * @param request request.getParameter("idProfile")
     * @return JSONObject
     * @throws com.google.appengine.labs.repackaged.org.json.JSONException
     * @Send message to the destiny
     */
    public static final JSONObject getMessages(HttpServletRequest request) throws JSONException {
        JSONObject js = new JSONObject();
        pm = PMF.get().getPersistenceManager();

        //Locate PushDevice from destiny message
        Query qPush = pm.newQuery(Chat.class);
        String pQuery = "to == :to";
        qPush.setFilter(pQuery);
        Long to = Long.parseLong(request.getParameter("idProfile"));
        List<Chat> lRet = (List<Chat>) qPush.execute(to);

        //Ordena conforme COmparable implementado
        Collections.sort(lRet);

        //Monta a lista
        JSONArray ja = new JSONArray();
        for (Chat c : lRet) {
            JSONObject chat = new JSONObject();
            chat.put("tit", c.getTit());
            chat.put("msg", c.getMsg());
            chat.put("data", c.getTimestampChat().toLocaleString());

            Perfil p = pm.getObjectById(Perfil.class, c.getFrom());
            chat.put("profile", p.getNome());

            ja.put(chat);
        }
        js.put("result", ja);
        pm.close();
        return js;
    }

    /**
     * @param request request.getParameter("idProfile")
     * @return JSONObject
     * @throws com.google.appengine.labs.repackaged.org.json.JSONException
     * @Send message to the destiny
     */
    public static final JSONObject getPromos(HttpServletRequest request) throws JSONException {
        JSONObject js = new JSONObject();
        pm = PMF.get().getPersistenceManager();

        Query qPush = pm.newQuery(Promocao.class);
        List<Promocao> lRet;
        if (request.getParameter("idProfile") != null) {
            String pQuery = "from == :from";
            qPush.setFilter(pQuery);
            Long to = Long.parseLong(request.getParameter("idProfile"));
            qPush.setFilter(pQuery);
            lRet = (List<Promocao>) qPush.execute(to);
        } else {
            lRet = (List<Promocao>) qPush.execute();
        }

        //Ordena conforme COmparable implementado
        Collections.sort(lRet);

        //Monta a lista
        JSONArray ja = new JSONArray();
        for (Promocao c : lRet) {
            JSONObject promo = new JSONObject();
            promo.put("tit", c.getTit());
            promo.put("msg", c.getMsg());
            promo.put("data", c.getTimestampPromo().toLocaleString());

            Perfil p = pm.getObjectById(Perfil.class, c.getFrom());
            promo.put("author", p.getNome());
            promo.put("email", p.getEmail());

            ja.put(promo);
        }
        js.put("result", ja);
        pm.close();
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

    /**
     * Associa os contatos ao perfil
     *
     * @param request
     * @return
     * @throws com.google.appengine.labs.repackaged.org.json.JSONException
     */
    public static JSONObject createContact(HttpServletRequest request) throws JSONException {
        JSONObject js = new JSONObject();
        pm = PMF.get().getPersistenceManager();
        Long idProfile = Long.parseLong(request.getParameter("idProfile"));

        Contato contact = new Contato();
        contact.setPerfil(idProfile);

        js.put("idOwner", idProfile);
        JSONArray ja = new JSONArray();
        String[] lContacts = request.getParameter("contacts").split(",");
        ArrayList<Long> lIds = new ArrayList<Long>();
        for (String id : lContacts) {
            lIds.add(Long.parseLong(id));
            ja.put(Long.parseLong(id));
        }
        
        contact.getlPropriedades().addAll(lIds);
        pm.makePersistent(contact);
        
        js.put("contactsAdded", ja);
        pm.close();
        return js;
    }

}
