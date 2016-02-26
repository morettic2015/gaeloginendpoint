/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

import br.com.morettic.gaelogin.smartcities.vo.Imagem;
import br.com.morettic.gaelogin.smartcities.vo.Ocorrencia;
import br.com.morettic.gaelogin.smartcities.vo.Perfil;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import java.util.Date;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static br.com.morettic.gaelogin.smartcities.control.URLReader.*;
import br.com.morettic.gaelogin.smartcities.vo.TipoOcorrencia;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.sun.media.jfxmedia.logging.Logger;
import com.sun.org.apache.bcel.internal.generic.L2D;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import mediautil.gen.Log;

/**
 *
 * @author LuisAugusto
 */
public class PerfilControler {

    private static PersistenceManager pm = null;
    private static BlobstoreService blobstoreService;

    /**
     *
     * http://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=1&titulo=teste&lat=25.1&lon=-22.1&desc=123&idPic=1&tipo=EDUCACAO&idProfile=5664248772427776
     *
     */
    public static JSONObject saveOcorrencia(HttpServletRequest req, HttpServletResponse res) throws JSONException {
        JSONObject js = new JSONObject();

        pm = PMF.get().getPersistenceManager();

        Perfil p1 = pm.getObjectById(Perfil.class, new Long(req.getParameter("idProfile")));

        Ocorrencia ocorrencia = new Ocorrencia();
        //SEt attrs
        ocorrencia.setTitulo(req.getParameter("titulo"));
        ocorrencia.setDtOcorrencia(new Date());
        ocorrencia.setLatitude(req.getParameter("lat"));
        ocorrencia.setLongitude(req.getParameter("lon"));
        ocorrencia.setPerfil(new Long(req.getParameter("idProfile")));
        ocorrencia.setDescricao(req.getParameter("desc"));
        ocorrencia.setIp(getClientIpAddress(req));
        ocorrencia.setAvatar(new Long(req.getParameter("idPic")));

        //Enum
        TipoOcorrencia tp = TipoOcorrencia.valueOf(req.getParameter("tipo"));
        ocorrencia.setTipo(tp);

        //http://maps.googleapis.com/maps/api/geocode/json?latlng=-27.35,-48.32&sensor=true @todo ler dados e setar pais e cidade....
        ocorrencia.setCidade(null);
        ocorrencia.setState(null);
        ocorrencia.setCountry("PT_BR");

        try {
            //Salva ocorrencia
            pm.makePersistent(ocorrencia);
            //Adiciona o ID da ocorrencia na lista do usuario
            p1.getlOcorrencias().add(ocorrencia.getKey());
            pm.makePersistent(p1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            pm.close();
        }

        js.put("key", ocorrencia.getKey());
        js.put("titulo", ocorrencia.getTitulo());

        return js;
    }

    /**
     *
     * http://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=2&iName=malacma@gmail.com.br&iToken=1
     */
    public static JSONObject saveImagem(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject js = new JSONObject();

        Imagem i = new Imagem();
        // i.setKey(1l);
        i.setPath(request.getParameter("iName"));
        i.setImage(request.getParameter("iToken"));

        try {
            pm = PMF.get().getPersistenceManager();
            pm.makePersistent(i);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pm.close();
        }

        js.put("key", i.getKey());
        js.put("path", i.getPath());
        js.put("token", i.getImage());

        return js;
    }

    /**
     *
     * http://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=3&email=malacma@gmail.com.br&avatar=1&nome=Moretto&cpfCnpj=028.923.629-14&cep=88020100&passwd=1234&complemento=123&pjf=false&nasc=29/04/1979&id=-1
     */
    public static JSONObject savePerfil(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject js = new JSONObject();

        //Set fields
        Perfil p = new Perfil();
        String id = request.getParameter("id");
        if (!id.equals("-1")) {
            p.setKey(Long.parseLong(id));
        }

        p.setEmail(request.getParameter("email"));
        p.setNome(request.getParameter("nome"));
        p.setCpfCnpj(request.getParameter("cpfCnpj"));
        p.setAvatar(Long.parseLong(request.getParameter("avatar")));
        p.setCep(request.getParameter("cep"));
        p.setPassWd(request.getParameter("passwd"));
        p.setComplemento(request.getParameter("complemento"));
        p.setPais("PT_BR");
        p.setEhPessoaFisica(request.getParameter("pjf"));
        p.setNascimento(request.getParameter("nasc"));

        //ABre dados do endereço baseado no cep
        JSONObject address = readJSONUrl("https://viacep.com.br/ws/" + request.getParameter("cep") + "/json/");

        //Se tem o attr logradouro retornou o endereço baseado no cep....
        if (address.has("logradouro")) {
            p.setRua(address.getString("logradouro"));
            p.setCidade(address.getString("localidade"));
            p.setBairro(address.getString("bairro"));
        }
        //persiste objeto
        try {
            pm = PMF.get().getPersistenceManager();
            pm.makePersistent(p);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pm.close();
        }

        js.put("key", p.getKey());
        js.put("path", p.getEmail());

        return js;
    }

    public static JSONObject getUploadPath(HttpServletRequest req, HttpServletResponse res) throws JSONException {
        JSONObject js = new JSONObject();
        blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

        js.put("uploadPath", blobstoreService.createUploadUrl("/upload.exec").toString());

        return js;
    }

    public static JSONArray uploadImage(HttpServletRequest req, HttpServletResponse response) throws IOException {

        blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        //response.getWriter().print();
        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);

        /*Logger.logMsg(Log.LEVEL_WARNING, "IS EMPTY??" + blobs.isEmpty());
         System.out.print("IS EMPTY??" + blobs.isEmpty());*/
        //Set< String > set = blobs.keySet( );
        //Utils.log.log( Level.SEVERE, "UploadBlobHandler.doPost() size::"+set.size( ));
        // Iterator< String > iterator = set.iterator( );
        /*while ( iterator.hasNext( )) {
         Utils.log.log( Level.SEVERE, "UploadBlobHandler.doPost(): Blobstore key: "+iterator.next( ));
         }*/
        JSONArray ja = new JSONArray();
        List< BlobKey> list = blobs.get("myFile");

        for (int i = 0; list != null && i < list.size(); i++) {
            BlobKey key = list.get(i);
            // Utils.log.log( Level.SEVERE, "UploadBlobHandler.doPost(): Blobstore key: "+key.getKeyString( ));
            ja.put(key.getKeyString());
        }
        return ja;
    }

    /**
     *
     * http://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=4&id=5119667588825088
     *
     *
     */
    public static JSONObject findPerfilByIdOrEmail(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject js = new JSONObject();

        String id = request.getParameter("id");

        pm = PMF.get().getPersistenceManager();
        Perfil p = pm.getObjectById(Perfil.class, new Long(id));

        js.put("avatar", p.getAvatar());
        js.put("cep", p.getCep());
        js.put("complemento", p.getComplemento());
        js.put("cpfCnpj", p.getCpfCnpj());
        js.put("email", p.getEmail());
        js.put("key", p.getKey());
        js.put("nasc", p.getNascimento());
        js.put("nome", p.getNome());
        js.put("pass", p.getPassWd());
        js.put("configId", p.getConfig());

        return js;
    }

    public static void showImageById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BlobKey blobKey = new BlobKey(request.getParameter("blob-key"));
        blobstoreService.serve(blobKey, response);
    }

}
