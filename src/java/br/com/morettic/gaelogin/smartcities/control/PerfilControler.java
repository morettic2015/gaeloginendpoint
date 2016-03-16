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
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import static com.google.appengine.api.users.UserServiceFactory.getUserService;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.jdo.Query;

;

/**
 *
 * @author LuisAugusto
 */
public class PerfilControler {

    private static PersistenceManager pm = null;
    private static BlobstoreService blobstoreService;
    private static final Double UMK = 0.1570d;
    private static UserService userService;
    private static User user;

    /**
     *
     * http://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=1&titulo=teste&lat=25.1&lon=-22.1&desc=123&idPic=1&tipo=EDUCACAO&idProfile=5664248772427776
     *
     */
    public static JSONObject saveOcorrencia(HttpServletRequest req, HttpServletResponse res) throws JSONException {
        JSONObject js = new JSONObject();
        //Recupera o controle de transacao JDO
        pm = PMF.get().getPersistenceManager();
        //Recupera o perfil
        Perfil p1 = pm.getObjectById(Perfil.class, new Long(req.getParameter("idProfile")));
        //Cria nova ocorrencia
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

        //Outras fotos da ocorrência
        if (req.getParameter("idPic1") != null) {
            ocorrencia.setAvatar1(new Long(req.getParameter("idPic1")));
        }//Foto 2 opcional
        if (req.getParameter("idPic2") != null) {
            ocorrencia.setAvatar2(new Long(req.getParameter("idPic2")));
        }//Foto 3 opcional
        if (req.getParameter("idPic3") != null) {
            ocorrencia.setAvatar3(new Long(req.getParameter("idPic3")));
        }//Atualiza o endereço do usuario
        if (req.getParameter("address") != null) {
            ocorrencia.setAdress(req.getParameter("address"));
        }//SEgmento de dados para comaprtilhar apenas os publicos EX: cada prefeitura tem um token.... no sistema publico apenas os ALL....
        if (req.getParameter("segment") != null) {
            ocorrencia.setAdress(req.getParameter("segment"));
        }

        //Enum
        TipoOcorrencia tp = TipoOcorrencia.valueOf(req.getParameter("tipo"));
        ocorrencia.setTipo(tp);

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
        js.put("mine", p1.getEmail());

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
     *
     * @param request
     * @param response
     * @return JSONObject
     * @throws com.google.appengine.labs.repackaged.org.json.JSONException
     */
    public static JSONObject savePerfil(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject js = new JSONObject();
        pm = PMF.get().getPersistenceManager();
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
        JSONObject address = readJSONUrl(HTTPSVIACEPCOMBRWS + request.getParameter("cep") + "/json/");

        //Se tem o attr logradouro retornou o endereço baseado no cep....
        if (address.has("logradouro")) {
            p.setRua(address.getString("logradouro"));
            p.setCidade(address.getString("localidade"));
            p.setBairro(address.getString("bairro"));
        }
        //persiste objeto
        try {

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
    public static final String ERROR = "error";
    public static final String EMAIL_JÁ_EXISTE_NA_BASE_DE_DADOS = "email já existe na base de dados!";
    public static final String UM_FILHO_DA_PUTA_TENTOU_HACKER_OU_BUG_DE_ = "Um filho da puta tentou hacker ou bug de uma client maldito. Email ja existente porra!!!!";
    public static final String HTTPSVIACEPCOMBRWS = "https://viacep.com.br/ws/";

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
        Set<Ocorrencia> lSOcorrencias = new HashSet<Ocorrencia>();
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
         * Query q = pm.newQuery(Person.class, "(lastName == 'Smith' || lastName
         * == 'Jones')" + " && firstName == 'Harold'");
         */
        HashMap<String, String> mapaChaves = new HashMap<String, String>();
        if (request.getParameter("type") != null) {
            String[] types = request.getParameter("type").split(",");

            for (String tp : types) {
                mapaChaves.put(tp, tp);
            }
        }
        Query q = pm.newQuery(Ocorrencia.class);
        lSOcorrencias.addAll((List<Ocorrencia>) q.execute());

        JSONArray ja = new JSONArray();
        DateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        for (Ocorrencia o : lSOcorrencias) {
            //Latitude da ocorrencia
            Float mLatitude = Float.parseFloat(o.getLatitude());
            //Verifica se o tipo da ocorrencia está no mapa de chaves de tipo.
            //Se o mapa de chaves estiver vazio e nao tiver a chave nao faz nada 
            if (!mapaChaves.isEmpty() && !mapaChaves.containsValue(o.getTipo().name())) {
                continue;//Não e do tipo pesquisado
            }
            if (mLatitude >= latMin && mLatitude <= latMax) {
                //Monta o JSON
                JSONObject js1 = new JSONObject();
                js1.put("id", o.getKey());
                js1.put("ip", o.getIp());
                js1.put("lat", o.getLatitude());
                js1.put("lon", o.getLongitude());
                js1.put("tit", o.getTitulo());
                js1.put("desc", o.getDescricao());
                js1.put("tipo", o.getTipo().toString());
                js1.put("date", dt.format(o.getDtOcorrencia()));

                //Validar se nao tiver o avatar....
                //Recupera a imagem para associar o token do blob
                Imagem m = pm.getObjectById(Imagem.class, o.getAvatar());
                js1.put("token", m.getKey());

                //Imagens opcionais da ocorrência
                if (o.getAvatar1() != null) {
                    m = pm.getObjectById(Imagem.class, o.getAvatar1());
                    js1.put("token1", m.getKey());
                } else {
                    js1.put("token1", "null");
                }
                if (o.getAvatar2() != null) {
                    m = pm.getObjectById(Imagem.class, o.getAvatar2());
                    js1.put("token2", m.getKey());
                } else {
                    js1.put("token2", "null");
                }
                if (o.getAvatar3() != null) {
                    m = pm.getObjectById(Imagem.class, o.getAvatar3());
                    js1.put("token3", m.getKey());
                } else {
                    js1.put("token3", "null");
                }

                //Recupera o perfil
                Perfil pOcorencia = pm.getObjectById(Perfil.class, o.getPerfil());
                js1.put("author", pOcorencia.getNome());
                js1.put("email", pOcorencia.getEmail());

                //Recupera o avatar do usuario
                m = pm.getObjectById(Imagem.class, pOcorencia.getAvatar());
                js1.put("avatar", p.getAvatar());

                ja.put(js1);
            }
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
        String pQuery = "email == :pEmail";
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
     /*   if (p.size() < 1) {
         pNovo = new Perfil();
         pNovo.setEmail(email);
         pNovo.setPassWd(pass);
         pNovo.setEhPessoaFisica("true");
         pNovo.setNome("");
         pNovo.setNascimento("");
         pNovo.setCpfCnpj("");
         pNovo.setComplemento("");
         pNovo.setConfig(-1l);
         pNovo.setCep("");
         pm.makePersistent(pNovo);
         }*/
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

    public static void findImagemTokenById(HttpServletRequest request, HttpServletResponse response) throws JSONException, IOException {
        String id = request.getParameter("id");
        pm = PMF.get().getPersistenceManager();
        Imagem m = pm.getObjectById(Imagem.class, new Long(id));

        String imgToken = m.getImage().substring(0, m.getImage().length() - 2);
        imgToken = imgToken.substring(2);
        response.sendRedirect("infosegcontroller.exec?action=5&blob-key=" + imgToken);

    }

    public static JSONObject getProfileFromLDAP(HttpServletRequest request, HttpServletResponse response) {
        // Set up environment for creating initial context

        return new JSONObject();
    }

    /**
     *
     * http://api.openweathermap.org/data/2.5/weather?lat=-25&lon=-48&appid=0ac7c5066f0cba6f5bd7ceffb8cec5a0
     */
    public static JSONObject getWeatherInfoByLatLon(HttpServletRequest request, HttpServletResponse response) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Cria uma conta com base na conta do google
     */
    public static void autenticaUsuarioGoogle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        try {
            userService = UserServiceFactory.getUserService();
            user = userService.getCurrentUser();
            if (user == null) {
                response.sendRedirect(getUserService().createLoginURL(request.getRequestURI()));
            } else {
                pm = PMF.get().getPersistenceManager();
                //Set fields

                Query q = pm.newQuery(Perfil.class);
                String pQuery = "email == :pEmail";
                q.setFilter(pQuery);
                List<Perfil> lRet = (List<Perfil>) q.execute(user.getEmail().toUpperCase());
                //Nao tem nenhum com o email pesquisado....
                if ((lRet.size() <1)) {

                    Perfil p = new Perfil();
                    p.setEmail(user.getEmail());
                    p.setNome(user.getNickname());
                    p.setCpfCnpj("xxx.xxx.xxx-xx");
                    p.setAvatar(null);
                    p.setCep("88000-000");
                    p.setPassWd(user.getEmail());
                    p.setComplemento("N/I");
                    p.setPais("PT_BR");
                    p.setEhPessoaFisica("true");
                    p.setNascimento("dd/MM/YYYY");
                    p.setOrigem("GOOGLE");
                    //SALVA O NOVO USUARIO NA BASE.....
                    pm.makePersistent(p);
                }
                out.print("<h1>Sucesso</h1>");
                out.print("<p>Sua conta foi criada com sucesso. <br>Utilize seu email como senha no primeiro login e edite seu perfil.<br>Bem vindo a comunidade <b>SmartcitiesAPP</b> </p>");
            }
        } finally {
            out.close();

        }
    }

}
