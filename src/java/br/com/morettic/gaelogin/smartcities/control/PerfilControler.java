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
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.sun.media.jfxmedia.logging.Logger;
import com.sun.org.apache.bcel.internal.generic.L2D;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.jdo.Query;
import mediautil.gen.Log;

/**
 *
 * @author LuisAugusto
 */
public class PerfilControler {

    private static PersistenceManager pm = null;
    private static BlobstoreService blobstoreService;
    private static final Double UMK = 0.1570d;

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
    /*
     http://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=5&blob-key=AMIfv96deP5CQMlfG4sGdMKnSQSnBxz0AMSjALVRxpNn6XdYycaNR7UTUpRrbJxCpudfMAt3YRX2sWCXF_d8MJwGUOeeenlars60ba_FrAuHeXzsA1Ch6la1IZeAQ2v8x9r36PHC5EcfGiNw-gIDhr9LI9KTwnr_NJeciMwJCahWgVYrccqRhvE
     */

    public static void showImageById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BlobKey blobKey = new BlobKey(request.getParameter("blob-key"));

        /*ImagesService imagesService = ImagesServiceFactory.getImagesService();

         Image oldImage = ImagesServiceFactory.makeImageFromBlob(blobKey);
         Transform resize = ImagesServiceFactory.makeResize(150, 150);
        
         Image newImage = imagesService.applyTransform(resize, oldImage);

         byte[] newImageData = newImage.;*/
        blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
        blobstoreService.serve(blobKey, response);

    }

    /**
     * Query q = pm.newQuery(Person.class); q.setFilter("lastName ==
     * lastNameParam"); q.setOrdering("height desc");
     * q.declareParameters("String lastNameParam");
     *
     * try { List<Person> results = (List<Person>) q.execute("Smith"); if
     * (!results.isEmpty()) { for (Person p : results) { // Process result p } }
     * else { // Handle "no results" case } } finally { q.closeAll(); }
     *
     * q.setFilter("lastName == 'Smith' && height < maxHeight");
     */
    public static JSONObject findOcorrencias(HttpServletRequest request, HttpServletResponse response) throws JSONException, IOException {
        JSONObject js = new JSONObject();
        pm = PMF.get().getPersistenceManager();
        List<Long> lOcorrencias;
        List<Ocorrencia> lSOcorrencias = new ArrayList<Ocorrencia>();
        String id = request.getParameter("id");

        double lat = Double.parseDouble(request.getParameter("lat"));

        pm = PMF.get().getPersistenceManager();
        Perfil p = pm.getObjectById(Perfil.class, new Long(id));
        if (request.getParameter("mine") != null) {//
            lOcorrencias = p.getlOcorrencias();
            for (Long idOcorrencia : lOcorrencias) {
                Ocorrencia e = pm.getObjectById(Ocorrencia.class, idOcorrencia);
                lSOcorrencias.add(e);
            }
        }

        double q1 = 0.0d;

        if (request.getParameter("d").equals("50")) {
            q1 = (50 * UMK / 1000);
        } else if (request.getParameter("d").equals("20")) {
            q1 = (20 * UMK / 1000);
        } else if (request.getParameter("d").equals("10")) {
            q1 = (10 * UMK / 1000);
        } else {
            q1 = (100 * UMK / 1000);
        }
        double latMax, latMin;

        latMax = (lat + q1);
        latMin = (lat - q1);

        /**
         *
         * final Query query = pm.newQuery("SELECT FROM model.Strip WHERE
         * publishOn <= startDate
         * && endDate >= publishOn PARAMETERS Date startDate, Date endDate
         * import java.util.Date"); changed to
         *
         * final Query query = pm.newQuery("SELECT FROM model.Strip WHERE
         * this.publishOn >= startDate && this.publishOn <= endDate PARAMETERS
         * java.util.Date startDate, java.util.Date endDate")
         */
        //response.getWriter().println("\"latitude <= \" + latMax + \" && latitude >= \" + latMin");
        //@todo filter tipo
        // String query1 = "latitude >= '" + latMin + "' AND latitude <= '"+latMax +"'";
        //@todo filter tipo
        if (request.getParameter("type") != null) {

        }
        Query q = pm.newQuery(Ocorrencia.class);

        /* String pQuery = "latitude >= :lMin && latitude <= :lMax";
         q.setFilter(pQuery);*/
        //q.setFilter(query1);
        lSOcorrencias.addAll((List<Ocorrencia>) q.execute(latMin, latMax));

        JSONArray ja = new JSONArray();

        for (Ocorrencia o : lSOcorrencias) {

            JSONObject js1 = new JSONObject();
            js1.put("id", o.getKey());
            js1.put("ip", o.getIp());
            js1.put("lat", o.getLatitude());
            js1.put("lon", o.getLongitude());
            js1.put("tit", o.getTitulo());
            js1.put("desc", o.getDescricao());
            js1.put("tipo", o.getTipo().toString());

            //Recupera a imagem para associar o token do blob
            Imagem m = pm.getObjectById(Imagem.class, o.getAvatar());
            js1.put("token", m.getImage());

            //Recupera o perfil
            Perfil pOcorencia = pm.getObjectById(Perfil.class, o.getPerfil());
            js1.put("author", pOcorencia.getNome());
            js1.put("email", pOcorencia.getEmail());

            //Recupera o avatar do usuario
            m = pm.getObjectById(Imagem.class, pOcorencia.getAvatar());
            js1.put("avatar", m.getImage());

            ja.put(js1);
        }

        js.put("rList", ja);

        return js;
    }

    /**
     * http://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=7&email=malacma@hotmail.com&pass=jsjsjssss
     */
    public static JSONObject autenticaUsuario(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject js = new JSONObject();
        Perfil retorno = null;
        Perfil pNovo = null;
        String email = request.getParameter("email").toUpperCase();
        String pass = request.getParameter("pass");
        pm = PMF.get().getPersistenceManager();
        Query q = pm.newQuery(Perfil.class);
        String pQuery = "email >= :pEmail";
        q.setFilter(pQuery);
        boolean autenticado = false;
        List<Perfil> p = (List<Perfil>) q.execute(email);
        for (Perfil p1 : p) {
            if (p1.getPassWd().equalsIgnoreCase(pass)) {
                retorno = p1;
                autenticado = true;
                break;
            }
        }

        //Cria um perfil default.....
        if (p.size() < 1) {
            pNovo = new Perfil();
            pNovo.setEmail(email);
            pNovo.setPassWd(pass);
            pNovo.setEhPessoaFisica("true");

            pm.makePersistent(pNovo);
        }

        if (pNovo != null) {
            retorno = pNovo;
        } else if (retorno == null) {
            js.put("erro", "Usuário ou senha inválidos!");

            return js;
        }

        js.put("avatar", retorno.getAvatar());
        js.put("cep", retorno.getCep());
        js.put("complemento", retorno.getComplemento());
        js.put("cpfCnpj", retorno.getCpfCnpj());
        js.put("email", retorno.getEmail());
        js.put("key", retorno.getKey());
        js.put("nasc", retorno.getNascimento());
        js.put("nome", retorno.getNome());
        js.put("pass", retorno.getPassWd());
        js.put("configId", retorno.getConfig());
        js.put("pjf", retorno.isEhPessoaFisica());

        return js;
    }

    public static JSONObject findImagemTokenById(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        String id = request.getParameter("id");
        pm = PMF.get().getPersistenceManager();
        Imagem m = pm.getObjectById(Imagem.class, new Long(id));
        
        
        
        JSONObject js = new JSONObject();
        
        js.put("token",m.getImage());
        
        return js;
    }

}
