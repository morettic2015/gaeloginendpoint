/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

import br.com.morettic.gaelogin.InfoSegController;
import static br.com.morettic.gaelogin.InfoSegController.log;
import br.com.morettic.gaelogin.smartcities.vo.Imagem;
import br.com.morettic.gaelogin.smartcities.vo.Registro;
import br.com.morettic.gaelogin.smartcities.vo.Perfil;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import java.util.Date;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static br.com.morettic.gaelogin.smartcities.control.URLReader.*;
import br.com.morettic.gaelogin.smartcities.vo.Configuracao;
import br.com.morettic.gaelogin.smartcities.vo.Rating;
import br.com.morettic.gaelogin.smartcities.vo.RegistroAnonimo;
import br.com.morettic.gaelogin.smartcities.vo.RegistroMapeado;
import br.com.morettic.gaelogin.smartcities.vo.TipoEmail;
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
import java.util.Properties;
import java.util.Set;
import javax.jdo.Query;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.Collection;
import java.util.Random;

;

/**
 *
 * @author LuisAugusto
 */
public class PerfilController {

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
        Registro ocorrencia = new Registro();
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
//Seta como anonimo caso o parâmetro anonimo seja setado
            String anonimous = req.getParameter("anonimous");
            boolean isAnonimous = anonimous == null ? false : true;
            if (isAnonimous) {
                RegistroAnonimo registroAnonimo = makeRegistroAnonimo(ocorrencia);
                pm.makePersistent(registroAnonimo);
            }
            //ocorrencia.setAnonimous(isAnonimous);
            pm.close();
        }

        js.put("key", ocorrencia.getKey());
        js.put("titulo", ocorrencia.getTitulo());
        js.put("mine", p1.getEmail());

        return js;
    }

    private static final RegistroAnonimo makeRegistroAnonimo(Registro ocorrencia) throws JSONException {
        RegistroAnonimo registroAnonimo = new RegistroAnonimo();
        registroAnonimo.setKey(ocorrencia.getKey());
        registroAnonimo.setFakeRegister(ocorrencia.getKey());

        JSONObject fakeProfile = URLReader.readJSONUrl(HTTPSRANDOMUSERMEAPI);

        String fakeAvatar = fakeProfile.getJSONArray("results").getJSONObject(0).getJSONObject("picture").getString("large");
        String fakeEmail = fakeProfile.getJSONArray("results").getJSONObject(0).getString("email");
        String fakeName = fakeProfile.getJSONArray("results").getJSONObject(0).getJSONObject("name").getString("title");
        fakeName += " " + fakeProfile.getJSONArray("results").getJSONObject(0).getJSONObject("name").getString("first");
        fakeName += " " + fakeProfile.getJSONArray("results").getJSONObject(0).getJSONObject("name").getString("last");

        registroAnonimo.setFakeAvatar(fakeAvatar);
        registroAnonimo.setFakeEmail(fakeEmail);
        registroAnonimo.setFakeName(fakeName);

        fakeProfile = null;

        return registroAnonimo;
    }

    public static final String HTTPSRANDOMUSERMEAPI = "https://randomuser.me/api/";

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
        boolean ehNovo = true;
        String id = request.getParameter("id");
        if (!id.equals("-1")) {
            p = pm.getObjectById(Perfil.class, Long.parseLong(id));
            ehNovo = false;
            //p.setKey();
        } else {//Não tem como ter email repetido no DataStore
            Query qEmail = pm.newQuery(Perfil.class);
            String pQuery = "email == :pEmail";
            qEmail.setFilter(pQuery);
            String email = request.getParameter("email");
            List<Perfil> lRet = (List<Perfil>) qEmail.execute(email.toUpperCase());
            if (lRet.size() > 0) {
                ehNovo = false;
                p = lRet.get(0);
            }
        }

        p.setEmail(request.getParameter("email"));
        p.setNome(request.getParameter("nome"));
        p.setCpfCnpj(request.getParameter("cpfCnpj"));
        p.setAvatar(Long.parseLong(request.getParameter("avatar")));
        p.setCep(request.getParameter("cep"));
        p.setPassWd(request.getParameter("passwd"));
        p.setComplemento(request.getParameter("complemento"));

        p.setEhPessoaFisica(request.getParameter("pjf"));
        p.setNascimento(request.getParameter("nasc"));

        //ABre dados do endereço baseado no cep
        JSONObject address = readJSONUrl(HTTPSVIACEPCOMBRWS + request.getParameter("cep").replace("-", ""));

        p.setRua(address.getString("state"));
        p.setCidade(address.getString("city"));
        p.setBairro(address.getString("bairro"));
        p.setPais(address.getString("country"));
        //persiste objeto
        try {
            pm.makePersistent(p);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //Fecha a conexão com o datastore
            pm.close();
            //Coloca os parametros 
            js.put("key", p.getKey());
            js.put("path", p.getEmail());

            //Manda email de boas vindas!
            if (ehNovo) {
                //https://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=11&&tipo=NOVO_CADASTROemail="
                Queue queue = QueueFactory.getDefaultQueue();
                queue.add(TaskOptions.Builder.withUrl("/infosegcontroller.exec")
                        .param("action", "11")
                        .param("tipo", "NOVO_CADASTRO")
                        .param("email", p.getEmail())
                );
            }

            return js;
        }

    }
    public static final String ERROR = "error";
    public static final String EMAIL_JÁ_EXISTE_NA_BASE_DE_DADOS = "email já existe na base de dados!";
    public static final String UM_FILHO_DA_PUTA_TENTOU_HACKER_OU_BUG_DE_ = "Um filho da puta tentou hacker ou bug de uma client maldito. Email ja existente porra!!!!";
    public static final String HTTPSVIACEPCOMBRWS = "http://smartapp.morettic.com.br/postalcode/?code=";

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
     * @Filtra latitude e longitude
     */
    //http://www.myweather2.com/developer/forecast.ashx?uac=<your unique access code>&query=24.15,56.32&temp_unit=f
    public static JSONObject findOcorrencias(HttpServletRequest request, HttpServletResponse response) throws JSONException, IOException {
        JSONObject js = new JSONObject();
        pm = PMF.get().getPersistenceManager();
        Set<Registro> lSOcorrencias = new HashSet<Registro>();
        String id = request.getParameter("id");
        double lat = Double.parseDouble(request.getParameter("lat"));
        double lon = Double.parseDouble(request.getParameter("lon"));
        double latMax, latMin, q1;
        //Recupera a variação da latitude
        Integer distance = 0;
        //total tela
        int totaltela = 0;

        //recupera perfil
        Perfil p = pm.getObjectById(Perfil.class, new Long(id));

        try {
            distance = Integer.parseInt(request.getParameter("d"));
        } catch (NumberFormatException e) {
            distance = 10;//Distancia = 0 
        }

        //Calc da latitude variacao
        q1 = calcLat(distance);
        latMax = (lat + q1);
        latMin = (lat - q1);
        js.put("latMax", latMax);
        js.put("latMin", latMin);

        boolean searchImoveis = false, searchBeer = false, searchAlimentacao = false, searchMeio = false, searchCult = false, searchTransp = false, searchPol = false, searchTurismo = false, searchInfra = false, searchPolice = false, searchSaude = false, searchEduc = false, searchSpo = false;
        HashMap<String, String> mapaChaves = new HashMap<String, String>();
        if (request.getParameter("type") != null) {
            String[] types = request.getParameter("type").split(",");
            for (String tp : types) {
                if (tp.equals(TipoOcorrencia.IMOVEIS.toString())) {
                    searchImoveis = true;
                } else if (tp.equals(TipoOcorrencia.TURISMO.toString())) {
                    searchTurismo = true;
                } else if (tp.equals(TipoOcorrencia.BEER.toString())) {
                    searchBeer = true;
                } else if (tp.equals(TipoOcorrencia.INFRAESTRUTURA.toString())) {
                    searchInfra = true;
                } else if (tp.equals(TipoOcorrencia.SEGURANCA.toString())) {
                    searchPolice = true;
                } else if (tp.equals(TipoOcorrencia.SAUDE.toString())) {
                    searchSaude = true;
                } else if (tp.equals(TipoOcorrencia.EDUCACAO.toString())) {
                    searchEduc = true;
                } else if (tp.equals(TipoOcorrencia.ESPORTE.toString())) {
                    searchSpo = true;
                } else if (tp.equals(TipoOcorrencia.TRANSPORTE.toString())) {
                    searchTransp = true;
                } else if (tp.equals(TipoOcorrencia.POLITICA.toString())) {
                    searchPol = true;
                } else if (tp.equals(TipoOcorrencia.CULTURA.toString())) {
                    searchCult = true;
                } else if (tp.equals(TipoOcorrencia.MEIO_AMBIENTE.toString())) {
                    searchMeio = true;
                } else if (tp.equals(TipoOcorrencia.ALIMENTACAO.toString())) {
                    searchAlimentacao = true;
                }
                mapaChaves.put(tp, tp);
            }
        }

        /**
         * Filter
         */
        String filter = "this.latitude>=latMin && this.latitude<=latMax ";
        Query q = pm.newQuery(Registro.class, filter);
        q.declareParameters("Float latMin,Float latMax");
        //Adiciona todos
        lSOcorrencias.addAll((List<Registro>) q.execute(latMin, latMax));
        //se tiver checkado mine carrega 50 do cara que devem ser do mesmo tipo filtrado
        //Recupera as ocorrencias do perfil.
        if (request.getParameter("mine") != null) {//
            filter = "this.perfilUsuario==pPerfil";
            Query q2 = pm.newQuery(Registro.class, filter);
            q2.declareParameters("Long pPerfil");
            //q2.setOrdering("dtOcorrencia desc");
            q2.setRange(0, 50);//TOP 
            lSOcorrencias.addAll((Collection<? extends Registro>) q2.execute(p.getKey()));
        }
        //Lon max com base no raio da distância
        double lonMax = lon + (distance * 0.0009);
        double lonMin = lon - (distance * 0.0009);
        js.put("lonMax", lonMax);
        js.put("lonMin", lonMin);
        //Formata o resultado filtrado
        JSONArray ja = new JSONArray();
        DateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        for (Registro o : lSOcorrencias) {
            //Verifica se o tipo da ocorrencia está no mapa de chaves de tipo.
            //Se o mapa de chaves estiver vazio e nao tiver a chave nao faz nada 
            if (!mapaChaves.isEmpty() && !mapaChaves.containsValue(o.getTipo().name())) {
                continue;//Não e do tipo pesquisado
            }
            double mLon = new Double(o.getLongitude());
            if (!(mLon >= lonMin) && (mLon <= lonMax)) {
                continue;
            }

            //Monta o JSON
            JSONObject js1 = new JSONObject();
            js1.put("id", o.getKey());
            js1.put("ip", o.getIp());
            js1.put("lat", o.getLatitude());
            js1.put("lon", o.getLongitude());
            js1.put("tit", o.getTitulo());
            js1.put("desc", o.getDescricao() + " " + o.getAdress());
            js1.put("tipo", o.getTipo().toString());
            js1.put("date", dt.format(o.getDtOcorrencia()));

            totaltela++;

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
            js1.put("avatar", m.getKey());

            ja.put(js1);
            pOcorencia = null;
            m = null;

        }

        lSOcorrencias.clear();
        q = null;
        js.put("totalView", totaltela);
        js.put("rList", ja);
        /**
         * @TODO make filters based on the token from user. In the future i need
         * to make a HASHMAP for each language EACH STOP WORD NEED A TRANSLATION
         * IN A TABLE!!!!
         *
         *
         */
        //Carrega os imóveis
        //Le dados do genimo
        String city = request.getParameter("myCity");
        JSONArray jOpenStreetMap = new JSONArray();
        if (searchImoveis) {

            // I//nteger dImoveis = distance < 1000 ? 1000 : distance;
            //i*=2;
            GenimoController gc = new GenimoController(lat, lon, distance);
            ja = gc.doSearch();

            js.put("urlGenimo", gc.getUrl());
            js.put("iList", ja);
            js.put("totalBigData", totaltela);
            totaltela += ja.length();
            js.put("totalView", totaltela);
            js.put("totalImoveis", ja.length());

            jOpenStreetMap.put(getOpenStreeMapCollection(city, "IMOVEIS", TipoOcorrencia.IMOVEIS.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "IMOBILIARIA", TipoOcorrencia.IMOVEIS.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "CORRETORA", TipoOcorrencia.IMOVEIS.toString()));
        }

        if (searchTurismo) {
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "BAR", TipoOcorrencia.TURISMO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "HOTEL", TipoOcorrencia.TURISMO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "PUB", TipoOcorrencia.TURISMO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "IGREJA", TipoOcorrencia.TURISMO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "PARQUE", TipoOcorrencia.TURISMO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "MARINA", TipoOcorrencia.TURISMO.toString()));
        }
        if (searchPolice) {
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "POLICE", TipoOcorrencia.SEGURANCA.toString()));
        }
        if (searchAlimentacao) {
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "PIZZA", TipoOcorrencia.ALIMENTACAO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "CHURRASCARIA", TipoOcorrencia.ALIMENTACAO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "SUSHI", TipoOcorrencia.ALIMENTACAO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "BUFFET", TipoOcorrencia.ALIMENTACAO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "PADARIA", TipoOcorrencia.ALIMENTACAO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "SUPERMERCADO", TipoOcorrencia.ALIMENTACAO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "MERCADO", TipoOcorrencia.ALIMENTACAO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "RESTAURANTE", TipoOcorrencia.ALIMENTACAO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "BURGER", TipoOcorrencia.ALIMENTACAO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "CAFE", TipoOcorrencia.ALIMENTACAO.toString()));
        }
        if (searchMeio) {
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "MORRO", TipoOcorrencia.MEIO_AMBIENTE.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "COSTAO", TipoOcorrencia.MEIO_AMBIENTE.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "DUNAS", TipoOcorrencia.MEIO_AMBIENTE.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "LAGO", TipoOcorrencia.MEIO_AMBIENTE.toString()));
        }
        if (searchPol) {
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "MACONARIA", TipoOcorrencia.POLITICA.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "PREFEITURA", TipoOcorrencia.POLITICA.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "SECRETARIA", TipoOcorrencia.POLITICA.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "FEDERACAO", TipoOcorrencia.POLITICA.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "ADVOGADO", TipoOcorrencia.POLITICA.toString()));
        }
        if (searchBeer) {
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "BAR", TipoOcorrencia.BEER.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "CERVEJA", TipoOcorrencia.BEER.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "BEER", TipoOcorrencia.BEER.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "DRINK", TipoOcorrencia.INFRAESTRUTURA.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "SHOW", TipoOcorrencia.INFRAESTRUTURA.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "PUB", TipoOcorrencia.INFRAESTRUTURA.toString()));
        }
        if (searchInfra) {
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "RODOVIARIA", TipoOcorrencia.INFRAESTRUTURA.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "POSTO", TipoOcorrencia.INFRAESTRUTURA.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "BANCO", TipoOcorrencia.INFRAESTRUTURA.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "FUNDACAO", TipoOcorrencia.INFRAESTRUTURA.toString()));
        }
        if (searchSaude) {
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "SAUDE", TipoOcorrencia.SAUDE.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "UPA", TipoOcorrencia.SAUDE.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "FARMACIA", TipoOcorrencia.SAUDE.toString()));
        }
        if (searchCult) {
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "TEATRO", TipoOcorrencia.CULTURA.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "EVENTOS", TipoOcorrencia.CULTURA.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "CINEMA", TipoOcorrencia.CULTURA.toString()));
        }
        if (searchSpo) {
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "ESPORTE", TipoOcorrencia.ESPORTE.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "FUTEBOL", TipoOcorrencia.ESPORTE.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "TENIS", TipoOcorrencia.ESPORTE.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "SURF", TipoOcorrencia.ESPORTE.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "SKATE", TipoOcorrencia.ESPORTE.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "GOLF", TipoOcorrencia.ESPORTE.toString()));
        }
        if (searchTransp) {
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "TAXI", TipoOcorrencia.TRANSPORTE.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "TERMINAL", TipoOcorrencia.TRANSPORTE.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "PONTO DE ONIBUS", TipoOcorrencia.TRANSPORTE.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "RODOVIARIA", TipoOcorrencia.TRANSPORTE.toString()));
        }
        if (searchEduc) {
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "EDUCACAO", TipoOcorrencia.EDUCACAO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "UNIVERSIDADE", TipoOcorrencia.EDUCACAO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "FACULDADE", TipoOcorrencia.EDUCACAO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "ESCOLA", TipoOcorrencia.EDUCACAO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "CURSO", TipoOcorrencia.EDUCACAO.toString()));
            jOpenStreetMap.put(getOpenStreeMapCollection(city, "BIBLIOTECA", TipoOcorrencia.EDUCACAO.toString()));
        }
        //Merge all jsonobjects from openstreet into a single Array
        JSONArray openStreetFinal = new JSONArray();
        for (int i = 0; i < jOpenStreetMap.length(); i++) {
            ja = jOpenStreetMap.getJSONArray(i);
            for (int x = 0; x < ja.length(); x++) {
                openStreetFinal.put(ja.getJSONObject(x));
            }

        }
        jOpenStreetMap = null;

        js.put("openStreetSize", openStreetFinal.length());
        js.put("openStreet", openStreetFinal);

        //Adiciona cinco usuários anonimos para os dados abertos humanizar!
        JSONArray lUsers = new JSONArray();
        lUsers.put(getMyProfile());
        lUsers.put(getMyProfile());
        lUsers.put(getMyProfile());
        lUsers.put(getMyProfile());
        lUsers.put(getMyProfile());
        js.put("profiles", lUsers);

        pm.close();

        return js;
    }

    /**
     * DOes the fuck email exists?
     */
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
        pm.close();
        return js;
    }

    public static void findImagemTokenById(HttpServletRequest request, HttpServletResponse response) throws JSONException, IOException {
        String id = request.getParameter("id");
        pm = PMF.get().getPersistenceManager();
        Imagem m = pm.getObjectById(Imagem.class, new Long(id));

        String imgToken = m.getImage().substring(0, m.getImage().length() - 2);
        imgToken = imgToken.substring(2);
        response.sendRedirect("infosegcontroller.exec?action=5&blob-key=" + imgToken);
        // pm.close();

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

    public static JSONObject hasEmailIntoDataStore(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject js = new JSONObject();
        pm = PMF.get().getPersistenceManager();
        Query q = pm.newQuery(Perfil.class);
        String pQuery = "email == :pEmail";
        q.setFilter(pQuery);
        String email = request.getParameter("email");
        List<Perfil> lRet = (List<Perfil>) q.execute(email.toUpperCase());
        //Nao tem nenhum com o email pesquisado....
        if ((lRet.size() < 1)) {
            js.put("total", 0);
        } else {
            js.put("total", 1);
        }
        pm.close();
        return js;
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
                if ((lRet.size() < 1)) {

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
                out.print("https://www.googleapis.com/plus/v1/people/" + user.getUserId());
                out.print("<h1>Sucesso</h1>");
                out.print("<p>Sua conta foi criada com sucesso. <br>Utilize seu email como senha no primeiro login e edite seu perfil.<br>Bem vindo a comunidade <b>SmartcitiesAPP</b> </p>");
            }
        } finally {
            out.close();

        }
    }

    public static JSONObject sendGMail(HttpServletRequest request, HttpServletResponse response) {
        //Recupera o email da requisição
        String email = request.getParameter("email");
        //Conecta no banco
        pm = PMF.get().getPersistenceManager();
        Query q = pm.newQuery(Perfil.class);
        String pQuery = "email == :pEmail";
        q.setFilter(pQuery);
        String msgBody = "";
        List<Perfil> lRet = (List<Perfil>) q.execute(email.toUpperCase());
        //Nao tem nenhum com o email pesquisado....
        if ((lRet.size() < 1)) {
            return new JSONObject();
        }
        //Recupera o perfil
        Perfil p = lRet.get(0);

        TipoEmail tipoEmail = TipoEmail.valueOf(request.getParameter("tipo"));
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        try {
            //Cria mensagem
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("malacma@gmail.com", "Smartcities Framework - Cidades Inteligêntes"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email, p.getNome()));

            //Mensagem recuperar senha
            if (tipoEmail.equals(TipoEmail.RECUPERAR_SENHA)) {
                msg.setSubject("[SmartcitiesAPP - Recuperação de senha]");
                msgBody += "Conforme solicitado, abaixo segue sua senha";
                msgBody += "\n\nSenha:" + p.getPassWd();
                //Mensagem novo cadastro
            } else if (tipoEmail.equals(TipoEmail.NOVO_CADASTRO)) {
                msg.setSubject("[SmartcitiesAPP - Perfil cadastrado com sucesso]");
                msgBody += "\nBem vindo ao nosso aplicativo Smarticies APP. Seu perfil foi cadastrado com sucesso. Abaixo seguem suas informações. Mantenha seu perfil atualizado! Aproveite para registrar ocorrências em sua cidade.";
                msgBody += "\n\nNome:" + p.getNome();
                msgBody += "\n\nUsuario:" + p.getEmail();
                msgBody += "\n\nSenha:" + p.getPassWd();
            }
            msgBody += "\n\nhttp://morettic.com.br";
            msg.setText(msgBody);
            Transport.send(msg);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new JSONObject();
    }

    /**
     * @Param idOcorrencia:Long
     * @Param idPerfil:Long
     * @Param rating:Double
     * @Todo Criar indices no gae....
     * @Code 200 = Sucesso
     * @Code 404 = Perfil ou Ocorrencia nao encontrados....
     * @Url request https://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=13&idPerfil=5142768607297536&idOcorrencia=4822398406754304&rating=5.5
     */
    public static final JSONObject ocorrenciaRating(HttpServletRequest request, HttpServletResponse response) throws JSONException {

        JSONObject js = new JSONObject();
        Long idOcorrencia = new Long(request.getParameter("idOcorrencia"));
        Long idPerfil = new Long(request.getParameter("idPerfil"));
        Double rating = new Double(request.getParameter("rating"));

        pm = PMF.get().getPersistenceManager();

        js.put("rating", rating);
        js.put("idPerfil", idPerfil);
        js.put("idOcorrencia", idOcorrencia);

        try {
            Registro o1 = pm.getObjectById(Registro.class, idOcorrencia);
            Perfil p1 = pm.getObjectById(Perfil.class, idPerfil);
            js.put("msg", "Sucesso");
            js.put("code", "200");
        } catch (Exception e) {
            js.put("msg", "Ocorrência/Perfil inválidos!");
            js.put("code", "404");
            return js;
        }
        //Cria o rating valido no sistema....
        Rating r = new Rating();
        r.setIdOcorrencia(idOcorrencia);
        r.setIdProfile(idPerfil);
        r.setRating(rating);
        //Salva 
        pm.makePersistent(r);
        //Adiciona no catalogo!!!!!
        RatingSingleton.put(idOcorrencia, r.getKey(), rating);
        //Retorna completo
        return js;
    }

    public static Double calcLat(int distance) {
        return (Double) (distance * UMK / 1000);
    }

    public static JSONObject getProfileFromDeepWeb(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        String email = request.getParameter("email");
        JSONObject profile = new JSONObject();
        profile.put("email", email);
        profile.put("status", 404);

        //read full contact
        JSONObject fullContact = getFullContactJSON(email);
        if (fullContact.getInt("status") == 200) {//Encontrou
            profile.put("status", 200);
            if (fullContact.has("photos")) {
                JSONArray ja = fullContact.getJSONArray("photos");
                String avatar = "avatar";
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject js = ja.getJSONObject(i);
                    profile.put(avatar + "_" + i, js.getString("url"));
                }
            }
            if (fullContact.has("contactInfo")) {
                if (fullContact.getJSONObject("contactInfo").has("fullName")) {
                    profile.put("name", fullContact.getJSONObject("contactInfo").getString("fullName"));
                }
            }
        } else {
            //Read from pipl deepweb lol
            fullContact = getPiplUrlJSON(email);//Cota acabou nego tenta a outra cota diária....
            if (fullContact.getInt("@http_status_code") != 200) {
                fullContact = getPiplUrlJSON1(email);
            }
            if (fullContact.getInt("@http_status_code") == 200) {
                profile.put("status", 200);
                if (fullContact.has("possible_persons")) {
                    JSONArray ja = fullContact.getJSONArray("possible_persons");
                    String nome = ja.getJSONObject(0)
                            .getJSONArray("names")
                            .getJSONObject(0)
                            .getString("display");
                    profile.put("name", nome);

                    if (ja.getJSONObject(0).has("addresses")) {
                        profile.put("country", ja.getJSONObject(0).getJSONArray("addresses").getJSONObject(0).getString("country"));
                        profile.put("state", ja.getJSONObject(0).getJSONArray("addresses").getJSONObject(0).getString("state"));
                        profile.put("display", ja.getJSONObject(0).getJSONArray("addresses").getJSONObject(0).getString("display"));
                    }
                }
                if (fullContact.has("person")) {
                    JSONArray ja = fullContact.getJSONObject("person").getJSONArray("names");
                    String nome = ja.getJSONObject(0).getString("display");
                    profile.put("name", nome);
                    //Endereço
                    if (fullContact.getJSONObject("person").has("addresses")) {
                        ja = fullContact.getJSONObject("person").getJSONArray("addresses");
                        profile.put("country", fullContact.getJSONObject("person").getJSONArray("addresses").getJSONObject(0).getString("country"));
                        profile.put("city", fullContact.getJSONObject("person").getJSONArray("addresses").getJSONObject(0).getString("city"));
                        profile.put("display", fullContact.getJSONObject("person").getJSONArray("addresses").getJSONObject(0).getString("display"));
                    }
                }
            }
        }

        return profile;

    }

    private static final JSONObject getPiplUrlJSON(String email) {
        String url = "http://api.pipl.com/search/?email=" + email + "&key=CONTACT-DEMO-rh6e4asn0wb8vgmj6wa0umbv";
        return readJSONUrl(url);
    }

    private static final JSONObject getPiplUrlJSON1(String email) {
        String url = "http://api.pipl.com/search/?email=" + email + "&key=CONTACT--DEMO-97k50hjhg35m89cka8fjy6vd";
        return readJSONUrl(url);
    }

    private static final JSONObject getFullContactJSON(String email) {
        String url = "https://api.fullcontact.com/v2/person.json?email=" + email + "&apiKey=ba2fbd5adb0456e2";
        return readJSONUrl(url);
    }

    /**
     * @URL
     * https://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=15&idProfile=5083670394175488&phone=+55(48)96004929&props=a:b-c:d-e:F
     */
    public static JSONObject updateConfigInfoFromProfile(HttpServletRequest request, HttpServletResponse response) throws JSONException {

        pm = PMF.get().getPersistenceManager();
        String idProfile = request.getParameter("idProfile");
        Long id = new Long(idProfile);
        Perfil p = pm.getObjectById(Perfil.class, id);
        JSONObject js = new JSONObject();
        Configuracao cfg = null;

        try {
            cfg = pm.getObjectById(Configuracao.class, id);
            pm.deletePersistent(cfg);
        } catch (Exception e) {
        }
        //Novo objeto remove e insere novo
        cfg = new Configuracao();
        cfg.setKey(id);
        cfg.setOwner(id);
        //Recupera o cellphone
        String phone = request.getParameter("phone");
        cfg.setCellPhone(phone);
        //Propriedades
        String[] properties = request.getParameter("props").split("-");
        for (String str : properties) {
            String[] pairs = str.split(":");
            cfg.setPairValue(pairs[0], pairs[1]);
        }

        try {
            pm.makePersistent(cfg);

            js.put("phone", phone);
            js.put("id", cfg.getKey());
            js.put("pk", cfg.getOwner());

            JSONArray ja = new JSONArray();
            Set<String> c = cfg.getlPropriedades().keySet();
            for (String key : c) {
                JSONObject js2 = new JSONObject();
                js2.put(key, cfg.getValue(key));
                ja.put(js2);
            }
            js.put("cfg", ja);
            js.put("status", 200);
        } catch (Exception e) {
            js.put("status", 500);
            js.put("error", e.getLocalizedMessage());
        }
        return js;
    }

    /**
     *
     * @http://gaeloginendpoint.appspot.com/images/weather.png
     *
     */
    public static JSONObject findListOcorrenciasRecentes(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        //Conecta com o banco
        pm = PMF.get().getPersistenceManager();
        Query q = pm.newQuery("select from br.com.morettic.gaelogin.smartcities.vo.Registro order by dtOcorrencia desc");
        q.setRange(0, 10);

        //Inicializa Json
        JSONObject js = new JSONObject();
        List<Registro> lSOcorrencias = (List<Registro>) q.execute();

        //Executa apenas uma vez...
        RatingSingleton.init(pm);

        //Formata o resultado filtrado
        JSONArray ja = new JSONArray();

        //Imagem padrão
        Imagem m = null;
        DateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        for (Registro o : lSOcorrencias) {
            JSONObject js1 = new JSONObject();
            js1.put("id", o.getKey());
            js1.put("tit", o.getTitulo());
            js1.put("desc", o.getDescricao());
            js1.put("tipo", o.getTipo().toString());
            js1.put("date", dt.format(o.getDtOcorrencia()));
            js1.put("address", o.getAdress());
            js1.put("lat", o.getLatitude());
            js1.put("lon", o.getLongitude());
            js1.put("rating", RatingSingleton.getRating(o.getKey()));

            //Validar se nao tiver o avatar....
            //Recupera a imagem para associar o token do blob
            try {
                m = pm.getObjectById(Imagem.class, o.getAvatar());
                js1.put("token", m.getKey());
            } catch (javax.jdo.JDOObjectNotFoundException jDOObjectNotFoundException) {
                js1.put("token", "-1");
            }
            //Imagens adicionais e suas validações
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

            ja.put(js1);

        }

        String ct = Normalizer.normalize(request.getParameter("city"), Normalizer.Form.NFD);
        ct = ct.replaceAll("[^\\p{ASCII}]", "");

        js.put("rList", ja);
        js.put("wList", readJSONArrayUrl(getWebhoseIo(ct)));
        js.put("tList", readJSONArrayUrl(getTwitter(ct)));
        return js;
    }

    /**
     *
     * ?action=17&dataInfo=4id=??&avatar=?? Switch para inicializar os dados de
     * diferentes fontes.
     */
    public static JSONObject initDataStoreInfo(HttpServletRequest request, HttpServletResponse response) throws JSONException, IOException {
        int dataInfo = new Integer(request.getParameter("dataInfo"));
        JSONObject js = new JSONObject();
        switch (dataInfo) {
            case 1:
                int total = loadPostosDeSaude(request, response);
                js.put("total", total);
                js.put("action", "postos de saude inseridos como ocorrencias");
                break;
            case 2:
                total = removePostosSaude();
                js.put("total", total);
                js.put("action", "postos de saude removidos da base");
                break;
            case 3:
                total = loadUpaLocal(request, response);
                js.put("total", total);
                js.put("action", "UPAS ATUALIZADAS NA BASE");
                break;

            default:
                break;

        }

        return js;
    }

    private static int removePostosSaude() {
        pm = PMF.get().getPersistenceManager();

        Query q = pm.newQuery(Registro.class);

        List<Registro> lOcorrencias = (List<Registro>) q.execute();
        int total = 0;
        for (Registro o : lOcorrencias) {
            if (o.getTipo().name().equals("POSTO_SAUDE")) {
                pm.deletePersistent(o);
                total++;
            }
        }

        pm.close();

        return total;
    }

    //4877958103695360 avatar
    //5173176170446848 perfil
    /**
     *
     *
     * -23.896, vlr_latitude 1 -53.41, vlr_longitude 2 411885, cod_munic 3
     * 6811299, cod_cnes 4 UNIDADE DE ATENCAO PRIMARIA SAUDE DA FAMILIA,
     * nom_estab 5 RUA GUILHERME BRUXEL 6,CENTRO 7,Perobal 8,4436251462 9,
     * dsc_endereco dsc_bairro dsc_cidade dsc_telefone Desempenho muito acima da
     * mÃ©dia, dsc_estrut_fisic 10 Desempenho muito acima da mÃ©dia,ambiencia 11
     * Desempenho mediano ou um pouco abaixo da
     * mÃ©dia,dsc_adap_defic_fisic_idosos 12 Desempenho muito acima da mÃ©dia 13
     */
    private static int loadPostosDeSaude(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String murl = "http://repositorio.dados.gov.br/saude/unidades-saude/unidade-basica-saude/ubs.csv";
        BufferedReader reader = null;
        URL url;
        int total = 0;
        try {
            url = new URL(murl);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = null;

            line = reader.readLine();
            //IP
            String ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }

            //LInha a linha cria as ocorrencias
            while ((line = reader.readLine()) != null) {
                //Se der erro na linha continua na proxima
                try {
                    pm = PMF.get().getPersistenceManager();
                    String[] lRegister = line.split(",");
                    //Ocorrencia 
                    Registro o = new Registro();
                    o.setLatitude(lRegister[0].toUpperCase());
                    o.setLongitude(lRegister[1].toUpperCase());
                    o.setTitulo(lRegister[4].toUpperCase());
                    o.setDtOcorrencia(new Date());
                    o.setIp("localhost");
                    o.setAdress(lRegister[5].toUpperCase() + "," + lRegister[6].toUpperCase() + "," + lRegister[7].toUpperCase() + ",Fone:" + lRegister[8].toUpperCase());
                    o.setAvatar(5068776655552512l);
                    o.setPerfil(4520495223406592l);
                    o.setTipo(TipoOcorrencia.POSTO_SAUDE);
                    o.setDescricao(lRegister[9].toUpperCase());
                    o.setIp(ipAddress);
                    //Salva

                    pm.makePersistent(o);
                    pm.close();

                    total++;

                    o = null;
                    lRegister = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            reader.close();

        } catch (Exception e) {

            e.printStackTrace();

        } finally {
            //Persiste as ocorrências
            url = null;
            reader.close();
            pm.flush();
            //Total de ocorrencias;

            return total;
        }
    }

    /**
     *
     *
     *
     * features: [ { type: "Feature", properties: [ { gid: "7085400" }, {
     * co_cep: "83708-695" }, { uf: "PR" }, { cidade: "Araucária" }, {
     * no_fantasia: "UNIDADE DE PRONTO ATENDIMENTO DE ARAUCARIA" }, { no_bairro:
     * "COSTEIRA" }, { nu_endereco: "1" }, { no_logradouro: "RUA AUGUSTO RIBEIRO
     * DOS SANTOS" }, { nu_telefone: "39056313" }, { ano_upa_func: "2016" }, {
     * mes_upa_func: "2" }, { fonte_recurso: "MS" }, { porte: "3" } ], geometry:
     * { type: "Point", coordinates: [ -49.41, -25.593 ] } },
     *
     */
    private static int loadUpaLocal(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        JSONObject input = URLReader.readJSONUrl(HTTPI3GEOSAUDEGOVBRI3GEOOGCPHPSERVICE_WF_SV);
        JSONArray ja = input.getJSONArray("features");
        int total = 0;;
        for (int i = 0; i < ja.length(); i++) {
            //Cria o registro

            try {
                Registro r = new Registro();
                r.setAvatar(5068776655552512l);//USUARIO ADMIN
                r.setPerfil(4520495223406592l);//AVATAR ADMIN

                //Propriedades do registro
                JSONObject jsValues = ja.getJSONObject(i);
                JSONArray lProperties = jsValues.getJSONArray("properties");
                //Recover properties from service
                String cep = lProperties.getJSONObject(1).getString("co_cep");
                String uf = lProperties.getJSONObject(2).getString("uf");
                String cidade = lProperties.getJSONObject(3).getString("cidade");
                String no_fantasia = lProperties.getJSONObject(4).getString("no_fantasia");
                String no_bairro = lProperties.getJSONObject(5).getString("no_bairro");
                String nu_endereco = lProperties.getJSONObject(6).getString("nu_endereco");
                String no_logradouro = lProperties.getJSONObject(7).getString("no_logradouro");
                String nu_telefone = lProperties.getJSONObject(8).getString("nu_telefone");
                String ano_upa_func = lProperties.getJSONObject(9).getString("ano_upa_func");
                String mes_upa_func = lProperties.getJSONObject(10).getString("mes_upa_func");
                String fonte_recurso = lProperties.getJSONObject(11).getString("fonte_recurso");
                String porte = lProperties.getJSONObject(12).getString("porte");
                //TIpo da ocorrencia
                r.setTipo(TipoOcorrencia.UPA);
                r.setTitulo(no_fantasia);
                r.setDescricao("Ano/Mes de abertura:"
                        + ano_upa_func + " "
                        + mes_upa_func + " fonte de recursos:"
                        + fonte_recurso + " porte:"
                        + porte + ", fone:"
                        + nu_telefone);
                r.setAdress(no_logradouro + ","
                        + no_bairro + ","
                        + nu_endereco + ","
                        + cidade + ","
                        + cep + ","
                        + uf);

                //Local pega as coordenadas
                JSONObject pos = jsValues.getJSONObject("geometry");
                String latitude = pos.getJSONArray("coordinates").getString(1);
                String longitude = pos.getJSONArray("coordinates").getString(0);
                r.setLatitude(latitude);
                r.setLongitude(longitude);

                pm = PMF.get().getPersistenceManager();
                pm.makePersistent(r);
                pm.close();
                total++;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //Conecta

        return total;
    }
    public static final String HTTPI3GEOSAUDEGOVBRI3GEOOGCPHPSERVICE_WF_SV = "http://i3geo.saude.gov.br/i3geo/ogc.php?service=WFS&version=1.0.0&request=GetFeature&typeName=upa_funcionamento_cnes&outputFormat=JSON";

    public static JSONObject loadNuclearPowerPlants(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        String url = "https://sutikasipus.cartodb.com/api/v2/sql?q=select * from public.nuclear_power_stations_worldwide";

        long idProfile = Long.parseLong(request.getParameter("id"));
        long idAvatar = Long.parseLong(request.getParameter("avatar"));

        JSONObject js = URLReader.readJSONUrl(url);
        JSONArray rows = js.getJSONArray("rows");

        int total = rows.length();

        for (int i = 0; i < total; i++) {
            JSONObject powerPlant = rows.getJSONObject(i);

            Registro r1 = new Registro();

            r1.setKey(new Long(powerPlant.getString("cartodb_id")));
            r1.setLatitude(powerPlant.getString("latitude"));
            r1.setLongitude(powerPlant.getString("longitude"));
            r1.setAdress(powerPlant.getString("country"));
            r1.setDtOcorrencia(new Date());
            r1.setPerfil(idProfile);
            r1.setTitulo(powerPlant.getString("name"));
            r1.setAvatar(idAvatar);
            r1.setIp(getClientIpAddress(request));
            r1.setTipo(TipoOcorrencia.USINA_NUCLEAR);
            r1.setSegment("ALL");
            //Description
            StringBuilder sb = new StringBuilder();
            sb.append("Reatores ativos:");
            sb.append(powerPlant.getString("active_reactors"));
            sb.append(", inativos:");
            sb.append(powerPlant.getString("shut_down_reactors"));
            sb.append(", em construção:");
            sb.append(powerPlant.getString("reactors_under_construction"));

            //Dados mapeados da região
            RegistroMapeado registroMapeado = new RegistroMapeado(powerPlant.getLong("cartodb_id"),
                    powerPlant.getString("the_geom"),
                    powerPlant.getString("the_geom_webmercator"));

            try {
                pm = PMF.get().getPersistenceManager();
                pm.makePersistent(r1);
                pm.makePersistent(registroMapeado);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return js;
    }

    /**
     * http://nominatim.openstreetmap.org/search?q=hotel%20florianopolis&format=json&polygon=0&addressdetails=1
     *
     *
     *
     * result: [ { icon:
     * "http://nominatim.openstreetmap.org/images/mapicons/accommodation_hotel2.p.20.png",
     * display_name: "Hotel Porto da Ilha, Rua Dom Jaime C��mara, Morro da
     * Mariquinha, Centro, Florian��polis, Microrregi��o de Florian��polis,
     * Mesorregi��o da Grande Florian��polis, Santa Catarina, South Region,
     * 88015-530, Brazil", licence: "Data �� OpenStreetMap contributors, ODbL
     * 1.0. http://www.openstreetmap.org/copyright", place_id: "118437043", lon:
     * "-48.5520058498876", importance: 0.101, address: { region: "Mesorregi��o
     * da Grande Florian��polis", county: "Microrregi��o de Florian��polis",
     * suburb: "Centro", state: "Santa Catarina", road: "Rua Dom Jaime C��mara",
     * hotel: "Hotel Porto da Ilha", postcode: "88015-530", country_code: "br",
     * neighbourhood: "Morro da Mariquinha", country: "Brazil", city:
     * "Florian��polis" }, boundingbox: [ "-27.5926301", "-27.5923632",
     * "-48.5520729", "-48.5519388" ], osm_id: "227073641", class: "tourism",
     * osm_type: "way", type: "hotel", lat: "-27.5924966" },
     *     
* {
     * id: 5110511926509568, author: "LAM MXRETTX", lon: "-48.67482", desc:
     * "CLÍNICO GERAL", token3: "null", token: 5673461879930880, address:
     * "ESTRADA GRP | 396 (IBIRAQUERA | PRAIA ROSA |
     * OUVIDOR),NULL,21,IMBITUBA,88495-000,STATE OF SANTA CATARINA,BRAZIL",
     * token1: "null", token2: "null", tipo: "SAUDE", tit: "CONSULTÓRIO
     * ODONTOLÓGICO ", rating: 3.5, date: "2016-09-11 07:45", lat: "-28.101107"
     * },
     *
     * @Search open stree map
     */
    public static JSONObject findOpenStreeMapInfo(HttpServletRequest request, HttpServletResponse response) throws JSONException {
        String city = request.getParameter("city");
        String service = request.getParameter("service");
        //Monta URL
        JSONObject js = new JSONObject();
        js.put("result", getOpenStreeMapCollection(city, service, "SEARCH"));
        js.put("profile", getMyProfile());
        return js;
    }
    private static HashMap<Long, JSONObject> cKey = new HashMap<Long, JSONObject>();

    private static JSONArray getOpenStreeMapCollection(String city, String service, String type) {

        JSONArray ja, ja2 = new JSONArray();

        //Le a URL
        try {

            ja = URLReader.readJSONArrayUrl(URLReader.getUrlOpenStreetMap(city, service));

            for (int i = 0; i < ja.length(); i++) {

                JSONObject js1 = new JSONObject(), js2;
                //Pega do openstreetmap
                js2 = ja.getJSONObject(i);

                //No duplicate from differente categories.
                //We may got an interssection so this avoid...
                if (cKey.containsKey(js2.getLong("place_id"))) {
                    ja2.put(cKey.get(js2.getLong("place_id")));
                    continue;
                }

                //trata
                String token = js2.has("icon") ? js2.getString("icon") : "default";
                String display_name = js2.getString("display_name");
                String display_nameV[] = display_name.split(",");
                StringBuilder addrs = new StringBuilder();
                if (js2.getJSONObject("address").has("road")) {
                    addrs.append(js2.getJSONObject("address").getString("road"));
                    addrs.append(" ");
                }
                if (js2.getJSONObject("address").has("house_number")) {
                    addrs.append(js2.getJSONObject("address").getString("house_number"));
                    addrs.append(" ");
                }
                if (js2.getJSONObject("address").has("postcode")) {
                    addrs.append(js2.getJSONObject("address").getString("postcode"));
                    addrs.append(" ");
                }
                if (js2.getJSONObject("address").has("suburb")) {
                    addrs.append(js2.getJSONObject("address").getString("suburb"));
                    addrs.append(" ");
                }
                if (js2.getJSONObject("address").has("city")) {
                    addrs.append(js2.getJSONObject("address").getString("city"));
                    addrs.append(" ");
                }
                if (js2.getJSONObject("address").has("state")) {
                    addrs.append(js2.getJSONObject("address").getString("state"));
                    addrs.append(" ");
                }
                if (js2.getJSONObject("address").has("county")) {
                    addrs.append(js2.getJSONObject("address").getString("county"));
                }
                //Randon rating
                Random r = new Random();
                double rate = 0.0 + (5.0 - 0.0) * r.nextDouble();

                //seta
                js1.put("id", js2.getLong("place_id"));
                js1.put("lon", js2.getString("lon"));
                js1.put("desc", js2.get("class") + " - " + js2.get("type"));
                //js1.put("token3", null);

                js1.put("token", token);
                //js1.put("token", null);
                js1.put("address", addrs.toString());
                //js1.put("token1", null);
                //js1.put("token2", null);
                js1.put("tipo", type);
                js1.put("tit", display_nameV[0]);
                js1.put("rating", rate);
                js1.put("date", new Date());
                js1.put("lat", js2.getString("lat"));

                //coloca na saida
                ja2.put(js1);
                cKey.put(js2.getLong("place_id"), js1);

            }

        } catch (Exception e) {
            JSONObject js = new JSONObject();

            js.put("500", e.toString());
            ja2.put(js);
        } finally {
            return ja2;
        }
    }

    /**
     *
     * name: { title: "miss", first: "kristin", last: "perry"
     *     
* location: { street: "2123 avondale ave", city: "grapevine", state:
     * "alaska", postcode: 19219 },
     *
     */
    private static JSONObject getMyProfile() throws JSONException {
        JSONObject perfil = new JSONObject();
        JSONObject fake = URLReader.readJSONUrl(HTTPSRANDOMUSERMEAPI);
        JSONObject fakeOne = fake.getJSONArray("results").getJSONObject(0);

        perfil.put("name", fakeOne.getJSONObject("name").getString("title")
                + "," + fakeOne.getJSONObject("name").getString("first")
                + "," + fakeOne.getJSONObject("name").getString("last"));

        perfil.put("location", fakeOne.getJSONObject("location").getString("street")
                + "," + fakeOne.getJSONObject("location").getString("city")
                + "," + fakeOne.getJSONObject("location").getString("state")
                + "," + fakeOne.getJSONObject("location").getString("postcode"));

        perfil.put("email", fakeOne.getString("email"));
        perfil.put("phone", fakeOne.getString("phone"));
        perfil.put("avatar", fakeOne.getJSONObject("picture").getString("thumbnail"));

        return perfil;
    }

    /**
     * @recover all favorites from one
     */
    public static JSONObject getMyFavorites(HttpServletRequest request) throws JSONException {
        pm = PMF.get().getPersistenceManager();
        Query q = pm.newQuery(Rating.class);
        String pQuery = "idProfile == :idProfile";
        q.setFilter(pQuery);
        DateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        List<Rating> lRet = (List<Rating>) q.execute(Long.parseLong(request.getParameter("id")));
        JSONObject jsRet = new JSONObject();
        JSONArray jaArr = new JSONArray();
        for (Rating r : lRet) {
            Registro o = pm.getObjectById(Registro.class, r.getIdOcorrencia());
            //Monta o JSON
            JSONObject js1 = new JSONObject();
            js1.put("id", o.getKey());
            js1.put("ip", o.getIp());
            js1.put("lat", o.getLatitude());
            js1.put("lon", o.getLongitude());
            js1.put("tit", o.getTitulo());
            js1.put("desc", o.getDescricao() + " " + o.getAdress());
            js1.put("tipo", o.getTipo().toString());
            js1.put("date", dt.format(o.getDtOcorrencia()));

            Imagem m;
            try {
                m = pm.getObjectById(Imagem.class, o.getAvatar());
                js1.put("token", m.getKey());
            } catch (javax.jdo.JDOObjectNotFoundException jDOObjectNotFoundException) {
                js1.put("token", "-1");
            }
            //Imagens adicionais e suas validações
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

            jaArr.put(js1);
        }

        jsRet.put("myFav", jaArr);
        pm.close();
        return jsRet;
    }

    /**
     * http://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=7&email=malacma@hotmail.com&pass=jsjsjssss
     */
    public static JSONObject perfilExists(HttpServletRequest request, JSONObject js1) throws JSONException {
        JSONObject js = js1;
        Perfil retorno = null;

        String email = request.getParameter("email").toUpperCase();

        pm = PMF.get().getPersistenceManager();
        Query q = pm.newQuery(Perfil.class);
        String pQuery = "email == :pEmail";
        q.setFilter(pQuery);

        List<Perfil> p = (List<Perfil>) q.execute(email);
        retorno = p.size() > 0 ? p.get(0) : null;

        if (retorno != null) {
            js.put("exists", true);
            js.put("avatar", retorno.getAvatar());
            js.put("cep", retorno.getCep());
            js.put("complemento", retorno.getComplemento());
            js.put("cpfCnpj", retorno.getCpfCnpj());
            js.put("email", retorno.getEmail());
            js.put("key", retorno.getKey());
            js.put("nasc", retorno.getNascimento());
            js.put("nome", retorno.getNome());
            //js.put("pass", retorno.getPassWd());
            js.put("configId", retorno.getConfig());
            js.put("pjf", retorno.isEhPessoaFisica());
            js.put("cidade", retorno.getCidade());
            js.put("pais", retorno.getPais());
            js.put("bairro", retorno.getBairro());
            js.put("rua", retorno.getRua());

            try {
                JSONArray ja = new JSONArray();
                Configuracao cfg = pm.getObjectById(Configuracao.class, retorno.getKey());
                js.put("cell", cfg.getCellPhone());
                js.put("push", cfg.getPushEnabled());
                Set<String> mKeys = cfg.getlPropriedades().keySet();

                for (String s : mKeys) {
                    ja.put(cfg.getlPropriedades().get(s));
                }
                js.put("config", ja);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            js.put("exists", false);
        }
        pm.close();
        return js;
    }

    public static JSONObject getMyWebsiteProfile(HttpServletRequest request, JSONObject types) throws JSONException {
        pm = PMF.get().getPersistenceManager();

        JSONObject js = types;

        Long id = Long.parseLong(request.getParameter("id"));
        Perfil p = pm.getObjectById(Perfil.class, id);

        js.put("cep", p.getCep());
        js.put("rua", p.getRua());
        js.put("bairro", p.getBairro());
        js.put("cidade", p.getCidade());
        js.put("pais", p.getPais());
        //pm.close();
        return js;

    }

    public static JSONObject getMyExperiences(HttpServletRequest request) throws JSONException {
        pm = PMF.get().getPersistenceManager();

        JSONObject js = new JSONObject();
        JSONArray ja = new JSONArray();

        Long id = Long.parseLong(request.getParameter("id"));
        Perfil p = pm.getObjectById(Perfil.class, id);

        List<Long> ids = p.getlIDsOcorrencias();
        for (Long idR : ids) {
            Registro r = pm.getObjectById(Registro.class, idR);

            JSONObject js1 = new JSONObject();

            js1.put("id", r.getKey());
            js1.put("lat", r.getLatitude());
            js1.put("lon", r.getLongitude());
            js1.put("tit", r.getTitulo());
            js1.put("desc", r.getDescricao());
            js1.put("tipo", r.getTipo().toString());
            js1.put("localizacao", r.getAdress());
            js1.put("dt", r.getDtOcorrencia());
            ja.put(js1);
        }

        js.put("result", ja);
        pm.close();
        return js;
    }

    public static JSONObject joinUs(HttpServletRequest request) throws JSONException {
        pm = PMF.get().getPersistenceManager();

        JSONObject js = new JSONObject();
        JSONArray ja = new JSONArray();

        Query q = pm.newQuery(Perfil.class);
        List<Perfil> joinUsNow = (List<Perfil>) q.execute();

        for (Perfil p : joinUsNow) {
            //Inicializa Json
            JSONObject js1 = new JSONObject();
            try {
                Imagem m = pm.getObjectById(Imagem.class, p.getAvatar());

                //TOken &
                js1.put("image", m.getImage());
                js1.put("path", m.getPath());
                js1.put("nick", p.getNome());

                ja.put(js1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        js.put("result", ja);
        pm.close();
        return js;
    }
}
