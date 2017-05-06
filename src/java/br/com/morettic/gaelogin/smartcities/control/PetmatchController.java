/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

import static br.com.morettic.gaelogin.smartcities.control.PerfilController.HTTPSVIACEPCOMBRWS;
import static br.com.morettic.gaelogin.smartcities.control.PerfilController.calcLat;
import static br.com.morettic.gaelogin.smartcities.control.URLReader.getClientIpAddress;
import static br.com.morettic.gaelogin.smartcities.control.URLReader.readJSONUrl;
import br.com.morettic.gaelogin.smartcities.vo.Chat;
import br.com.morettic.gaelogin.smartcities.vo.Configuracao;
import br.com.morettic.gaelogin.smartcities.vo.Contato;
import br.com.morettic.gaelogin.smartcities.vo.DeviceType;
import br.com.morettic.gaelogin.smartcities.vo.EspeciePet;
import br.com.morettic.gaelogin.smartcities.vo.Imagem;
import br.com.morettic.gaelogin.smartcities.vo.Perfil;
import br.com.morettic.gaelogin.smartcities.vo.Pet;
import br.com.morettic.gaelogin.smartcities.vo.PushDevice;
import br.com.morettic.gaelogin.smartcities.vo.Registro;
import br.com.morettic.gaelogin.smartcities.vo.TipoOcorrencia;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tools.ant.taskdefs.condition.Http;

/**
 *
 * @author LuisAugusto
 */
public class PetmatchController {

    private static PersistenceManager pm = null;
    /* private static BlobstoreService blobstoreService;
     private static final Double UMK = 0.1570d;
     private static UserService userService;
     private static User user;*/

    public static JSONObject updateProfile(HttpServletRequest req, HttpServletResponse res) throws JSONException, IOException {
        JSONObject js = new JSONObject();
        pm = PMF.get().getPersistenceManager();
        //Get parameters
        String id = req.getParameter("id");
        String nm = req.getParameter("name");
        String cep = req.getParameter("cep");
        String cpf = req.getParameter("cpf");
        String rua = req.getParameter("rua");
        String fone = req.getParameter("fone");
        String pass = req.getParameter("pass");
        //String bairro = req.getParameter("bairro");
        String complemento = req.getParameter("complemento");
        //CEP WEBSERVICE
        JSONObject address = readJSONUrl(HTTPSVIACEPCOMBRWS + cep.replace("-", ""));
        js.append("addrs", address);
        //Find perfil byID

        Query q = pm.newQuery(Perfil.class);
        String pQuery = "email == :pEmail";
        q.setFilter(pQuery);
        String email = req.getParameter("email");
        List<Perfil> lRet = (List<Perfil>) q.execute(email.toUpperCase());
        Perfil p1 = lRet.get(0);
        //Set profile
        p1.setRua(address.getString("state"));
        p1.setCidade(address.getString("city"));
        p1.setBairro(address.getString("bairro"));
        p1.setPais(address.getString("country"));
        p1.setNome(nm);
        p1.setRua(rua);
        p1.setCep(cep);
        p1.setCpfCnpj(cpf);
        p1.setComplemento(complemento);
        p1.setPassWd(pass);
        //COnfiguração novo ou nao
        Configuracao cfg = p1.getConfig() == null ? new Configuracao() : pm.getObjectById(Configuracao.class, p1.getConfig());
        //Update conf
        cfg.setCellPhone(fone);
        cfg.setOwner(p1.getKey());
        //Save conf
        pm.makePersistent(cfg);
        //Set p1 conf id
        p1.setConfig(cfg.getKey());
        //Save profile

        pm.makePersistent(p1);
        pm.close();

        js.put("conf", cfg.getKey());

        return js;
    }

    public static JSONObject loginProfile(HttpServletRequest req, HttpServletResponse res) throws JSONException, IOException {
        JSONObject js = new JSONObject();
        pm = PMF.get().getPersistenceManager();
        Query q = pm.newQuery(Perfil.class);
        String pQuery = "email == :pEmail";
        q.setFilter(pQuery);
        String email = req.getParameter("email");
        List<Perfil> lRet = (List<Perfil>) q.execute(email.toUpperCase());
        //Nao tem nenhum com o email pesquisado....
        Perfil p1;
        String id = req.getParameter("id");
        js.put("in", false);
        if ((lRet.size() < 1)) {
            /**
             * Não existe veio do facebook
             */
            js.put("new", true);
            if (id != null) {

                p1 = new Perfil();
                p1.setKey(Long.parseLong(id));
                p1.setEmail(email);
                p1.setNome(req.getParameter("name"));
                p1.setPassWd(java.util.UUID.randomUUID().toString());
                p1.setOrigem("PET_MATCH");
                p1.setEhPessoaFisica("true");
                p1.setPais("BRASIL");

                String imgPath = "http://graph.facebook.com/" + req.getParameter("id") + "/picture?type=large";

                //URL url = new URL(imgPath);
                //InputStream input = url.openStream();
                //byte[] imageData = getBytesFromInputStream(input);
                //FileService fileService = FileServiceFactory.getFileService();
                //blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
                //blobstoreService.
                //BlobKey bkey = fileService.getBlobKey(file);
                pm.makePersistent(p1);

                Imagem i = new Imagem();
                i.setKey(p1.getKey());
                i.setImage(imgPath);
                i.setPath(imgPath);
                pm.makePersistent(i);

                p1.setAvatar(p1.getKey());
                pm.makePersistent(p1);

                js.put("in", true);
                js.put("name", p1.getNome());
                js.put("email", p1.getEmail());
                js.put("id", p1.getKey().toString());

            }
        } else {
            /**
             * Fazendo login ja e cadastrado
             */

            js.put("new", false);
            p1 = lRet.get(0);
            js.put("name", p1.getNome());
            js.put("email", p1.getEmail());
            js.put("id", p1.getKey().toString());
            js.put("cpf", p1.getCpfCnpj());
            js.put("cep", p1.getCep());
            js.put("rua", p1.getRua());
            js.put("bairro", p1.getBairro());
            js.put("complemento", p1.getComplemento());

            Configuracao cfg;
            if (p1.getConfig() != null) {
                try {
                    cfg = pm.getObjectById(Configuracao.class, p1.getConfig());
                    js.put("fone", cfg.getCellPhone()); //TODO FONE
                } catch (EntityNotFoundException e) {
                    cfg = new Configuracao();
                }
            } else {
                js.put("fone", ""); //TODO FONE
            }

            String passwd = req.getParameter("pass") == null ? "###################################################" : req.getParameter("pass");
            if (passwd.equals(p1.getPassWd())) {
                js.put("in", true);
            } else if (id != null) {
                js.put("in", true);
            } else {
                js.put("in", false);
            }
        }
        //pm.close();
        return js;
    }

    public static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();) {
            byte[] buffer = new byte[0xFFFF];

            for (int len; (len = is.read(buffer)) != -1;) {
                os.write(buffer, 0, len);
            }

            os.flush();

            return os.toByteArray();
        }
    }

    private static JSONObject createContato(PersistenceManager pm, Long idOwner, Long idDestiny) throws JSONException {

        Contato from;

        try {
            Query q = pm.newQuery(Contato.class);
            String pQuery = "perfil == :pperfil";
            q.setFilter(pQuery);
            List<Contato> ffrom = (List<Contato>) q.execute(idOwner);
            from = ffrom.get(0);
        } catch (Exception e) {
            from = new Contato();
            from.setPerfil(idOwner);
        }
        Iterator<Long> it = from.getlPropriedades().iterator();
        Set<Long> seLong = new HashSet<>();

        while (it.hasNext()) {
            seLong.add(it.next());
        }

        if (!seLong.contains(idDestiny)) {
            from.getlPropriedades().add(idDestiny);
            pm.makePersistent(from);
        }

        JSONArray ja = new JSONArray();

        for (Long i : seLong) {
            JSONObject js123 = new JSONObject();
            Perfil p1 = pm.getObjectById(Perfil.class, i);
            js123.put("getKey", p1.getKey().toString());
            js123.put("getNome", p1.getNome());

            Imagem m = pm.getObjectById(Imagem.class, p1.getAvatar());

            js123.put("getImage", m.getImage());
            js123.put("getPath", m.getPath());

            ja.put(js123);

        }
        JSONObject js = new JSONObject();
        js.put("contatos", ja);

        return js;
    }

    public static final JSONObject saveChat(HttpServletRequest req, HttpServletResponse res) throws JSONException {
        pm = PMF.get().getPersistenceManager();

        String message = req.getParameter("message");
        String from = req.getParameter("from");
        String to = req.getParameter("to");
        String pet = req.getParameter("pet");
        Long idFrom = new Long(from);
        JSONObject js = new JSONObject();
        Pet p = pm.getObjectById(Pet.class, new Long(pet));

        Long idTo = to == null ? p.getIdOwner() : new Long(to);

        js.put("idToShit", idTo);

        Chat c = new Chat(p.getTitulo(), message, from, idTo.toString(), pet);

        js.put("contactList", createContato(pm, idFrom, idTo));
        createContato(pm, idTo, idFrom);

        pm.makePersistent(c);
        pm.close();

        /**
         * Send push
         */
        pm = PMF.get().getPersistenceManager();
        try {
            PushDevice pd = pm.getObjectById(PushDevice.class, idTo);
            //PushDevice pd1 = pm.getObjectById(PushDevice.class, p.getIdOwner());

            js.put("device", PushController.sendOneSignalPushToUser(pd.getOneSignalID(), "Mensagem de Adoção", message));
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.WARNING, e.getMessage());
        }

        //Carrega as mensagem que o usuario enviou para esse pet
        String filter = "this.petKey==petKey";
        Query q = pm.newQuery(Chat.class, filter);
        q.declareParameters("Float petKey");
        //Adiciona todos
        List<Chat> lChats = (List<Chat>) q.execute(p.getKey());
        //Adiciona na lista para ordenar
        //  List<Chat> lAll = new ArrayList((List<Chat>) lChats.iterator());
        //Ordena lista
        Collections.sort(lChats);
        JSONArray ja = new JSONArray();
        for (Chat c1 : lChats) {
            if (!((c1.getFrom().equals(idFrom) && c1.getTo().equals(idTo) || (c1.getFrom().equals(idTo) && c1.getTo().equals(idFrom))))) {
                continue;
            }
            JSONObject js1 = new JSONObject();

            js1.put("getKey", c1.getKey().toString());
            js1.put("getEnabled", c1.getEnabled());
            js1.put("getFrom", c1.getFrom().toString());
            js1.put("getMsg", c1.getMsg());
            js1.put("getTit", c1.getTit());
            js1.put("getTo", c1.getTo().toString());
            js1.put("getTimestampChat", c1.getTimestampChat());
            try {
                Perfil perfil = pm.getObjectById(Perfil.class, c1.getFrom());
                js1.put("getNome", perfil.getNome());
                Imagem m = pm.getObjectById(Imagem.class, perfil.getAvatar());

                String img = m.getImage().charAt(0) == '['
                        ? "https://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=5&blob-key=" + m.getImage().substring(2, +m.getImage().length() - 2)
                        : m.getImage();

                js1.put("getImage", img.replaceAll("http:", "https:"));
                js1.put("getPath", img.replaceAll("http:", "https:"));

            } catch (Exception e) {
                js1.put("getImage", "img/avatar.png");
                js1.put("getImage", "img/avatar.png");
            }

            ja.put(js1);
        }

        js.put("chats", ja);

        return js;

    }

    public static final JSONObject findPetsNearBy(HttpServletRequest req, HttpServletResponse res) throws JSONException {
        pm = PMF.get().getPersistenceManager();
        Set<Pet> lSOcorrencias = new HashSet<Pet>();
        //String id = request.getParameter("id");
        double lat = Double.parseDouble(req.getParameter("lat"));
        double lon = Double.parseDouble(req.getParameter("lon"));
        double latMax, latMin, q1;
        //Recupera a variação da latitude
        Integer distance = 0;

        //recupera perfil
        // Perfil p = pm.getObjectById(Perfil.class, new Long(id));
        try {
            distance = Integer.parseInt(req.getParameter("d"));
        } catch (NumberFormatException e) {
            distance = 10;//Distancia = 0
        }

        //Calc da latitude variacao
        q1 = calcLat(distance);
        latMax = (lat + q1);
        latMin = (lat - q1);

        String filter = "this.latitude>=latMin && this.latitude<=latMax ";
        Query q = pm.newQuery(Pet.class, filter);
        q.declareParameters("Float latMin,Float latMax");
        //Adiciona todos
        lSOcorrencias.addAll((List<Pet>) q.execute(latMin, latMax));
        JSONArray ja = new JSONArray();
        JSONObject js = new JSONObject();

        for (Pet pet : lSOcorrencias) {
            JSONObject j1 = new JSONObject();

            if (pet.getAdoptedBy() != null) {
                continue;
            }

            Imagem m = pm.getObjectById(Imagem.class, pet.getAvatar());

            j1.put("getAvatar", m.getPath());
            j1.put("getEspecie", pet.getEspecie().getId());
            j1.put("getIdade", pet.getIdade().toString());
            j1.put("getTitulo", pet.getTitulo());
            j1.put("getDescricao", pet.getDescricao());
            j1.put("getDtOcorrencia", pet.getDtOcorrencia().toString());
            j1.put("getPorte", pet.getPorte().toString());
            j1.put("getVacinado", pet.getVacinado().toString());
            j1.put("getCastrado", pet.getCastrado().toString());
            j1.put("getIdOwner", pet.getIdOwner().toString());
            j1.put("getSexo", pet.getSexo().toString());
            j1.put("id", pet.getKey().toString());
            ja.put(j1);

        }
        js.put("result", ja);

        return js;

    }

    public static final JSONObject removePet(HttpServletRequest req, HttpServletResponse res) throws JSONException {
        JSONObject js = new JSONObject();
        pm = PMF.get().getPersistenceManager();

        //Id Pet
        String idPet = req.getParameter("idPet");
        //Retrieve objetc toremove
        Pet p1 = pm.getObjectById(Pet.class, new Long(idPet));

        //Remove pet
        pm.deletePersistent(p1);
        pm.close();

        pm = PMF.get().getPersistenceManager();

        //Get id Owner
        String idOwner = req.getParameter("idOwner");

        String filter = "this.idOwner==id";
        Query q = pm.newQuery(Pet.class, filter);
        q.declareParameters("Long id");
        List<Pet> lPets = (List<Pet>) q.execute(new Long(idOwner));

        JSONArray ja = new JSONArray();
        for (Pet pet : lPets) {

            Imagem m = pm.getObjectById(Imagem.class, pet.getAvatar());
            JSONObject j1 = new JSONObject();
            j1.put("getAvatar", m.getPath());
            j1.put("getEspecie", pet.getEspecie().getId());
            j1.put("getIdade", pet.getIdade().toString());
            j1.put("getTitulo", pet.getTitulo());
            j1.put("getDescricao", pet.getDescricao());
            j1.put("getDtOcorrencia", pet.getDtOcorrencia().toString());
            j1.put("getPorte", pet.getPorte().toString());
            j1.put("getVacinado", pet.getVacinado().toString());
            j1.put("getCastrado", pet.getCastrado().toString());
            j1.put("getSexo", pet.getSexo().toString());
            j1.put("id", pet.getKey().toString());
            ja.put(j1);
        }

        js.put("mine", ja);

        pm.close();
        return js;
    }

    public static final JSONObject getMyPets(HttpServletRequest req, HttpServletResponse res) throws JSONException {

        pm = PMF.get().getPersistenceManager();
        //  pm.currentTransaction().commit();
        Long idOwner = Long.parseLong(req.getParameter("id"));
        String filter = "this.idOwner==id";
        Query q = pm.newQuery(Pet.class, filter);
        q.declareParameters("Long id");
        List<Pet> lPets = (List<Pet>) q.execute(idOwner);

        JSONArray ja = new JSONArray();
        for (Pet pet : lPets) {

            Imagem m = pm.getObjectById(Imagem.class, pet.getAvatar());
            JSONObject j1 = new JSONObject();
            j1.put("getAvatar", m.getPath());
            j1.put("getEspecie", pet.getEspecie().getId());
            j1.put("getIdade", pet.getIdade().toString());
            j1.put("getTitulo", pet.getTitulo());
            j1.put("getDescricao", pet.getDescricao());
            j1.put("getDtOcorrencia", pet.getDtOcorrencia().toString());
            j1.put("getPorte", pet.getPorte().toString());
            j1.put("getVacinado", pet.getVacinado().toString());
            j1.put("getCastrado", pet.getCastrado().toString());
            j1.put("getSexo", pet.getSexo().toString());
            j1.put("id", pet.getKey().toString());
            ja.put(j1);
        }
        JSONObject js = new JSONObject();
        js.put("mine", ja);

        pm.close();
        return js;
    }

    public static final JSONObject saveUpdatePet(HttpServletRequest req, HttpServletResponse res) throws JSONException {
        JSONObject js = new JSONObject();
        pm = PMF.get().getPersistenceManager();

        //Cria se for diferente de -1 recupera da Datastore
        Pet p = new Pet();
        if (!req.getParameter("id").equals("0")) {
            p = pm.getObjectById(Pet.class, new Long(req.getParameter("id")));
            // js.put("shitr", p.getKey());
        }
        p.setIdOwner(Long.parseLong(req.getParameter("idOwner")));
        p.setEnabledData(true);
        p.setCastrado(Integer.parseInt(req.getParameter("castrado")));
        p.setIdade(Integer.parseInt(req.getParameter("idade")));
        p.setPorte(Integer.parseInt(req.getParameter("porte")));
        p.setLatitude(req.getParameter("lat"));
        p.setLongitude(req.getParameter("lon"));
        p.setDtOcorrencia(new Date());
        p.setAvatar(1l);
        p.setTitulo(req.getParameter("tit"));
        p.setDescricao(req.getParameter("desc"));
        p.setIp(getClientIpAddress(req));
        p.setAdress("addrs");
        p.setPerfil(1l);
        p.setTipo(TipoOcorrencia.PET_MATCH);

        //Apenas cão e gato.....
        EspeciePet e1 = req.getParameter("especie").equals("1") ? EspeciePet.CAO : EspeciePet.GATO;

        p.setEspecie(e1);
        p.setSexo(Integer.parseInt(req.getParameter("sexo")));
        p.setVacinado(Integer.parseInt(req.getParameter("vacinado")));
        /**
         * @vincula o avatar do PET
         *
         */
        Imagem avatar = new Imagem();
        String avatarToken = req.getParameter("token");
        avatar.setPath("https://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=5&blob-key=" + avatarToken);
        avatar.setImage("https://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=5&blob-key=" + avatarToken);

        pm.makePersistent(avatar);

        p.setAvatar(avatar.getKey());

        pm.makePersistent(p);

        pm.close();

        pm = PMF.get().getPersistenceManager();
        //  pm.currentTransaction().commit();
        js.put("idPet", p.getKey().toString());
        js.put("idOwner", p.getIdOwner().toString());
        js.put("avatar", "https://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=5&blob-key=" + avatarToken);

        String filter = "this.idOwner==id";
        Query q = pm.newQuery(Pet.class, filter);
        q.declareParameters("Long id");
        List<Pet> lPets = (List<Pet>) q.execute(p.getIdOwner());

        JSONArray ja = new JSONArray();
        for (Pet pet : lPets) {

            Imagem m = pm.getObjectById(Imagem.class, pet.getAvatar());
            JSONObject j1 = new JSONObject();
            j1.put("getAvatar", m.getPath());
            j1.put("getEspecie", pet.getEspecie().getId());
            j1.put("getIdade", pet.getIdade().toString());
            j1.put("getTitulo", pet.getTitulo());
            j1.put("getDescricao", pet.getDescricao());
            j1.put("getDtOcorrencia", pet.getDtOcorrencia().toString());
            j1.put("getPorte", pet.getPorte().toString());
            j1.put("getVacinado", pet.getVacinado().toString());
            j1.put("getCastrado", pet.getCastrado().toString());
            j1.put("getSexo", pet.getSexo().toString());
            j1.put("id", pet.getKey().toString());
            ja.put(j1);
        }

        js.put("mine", ja);

        pm.close();
        return js;

    }

    public static final JSONObject registerUserDevice(HttpServletRequest req, HttpServletResponse res) throws JSONException {
        JSONObject js = new JSONObject();

        pm = PMF.get().getPersistenceManager();

        Long idUser = Long.parseLong(req.getParameter("id"));
        String token = req.getParameter("token");
        String oneSignalUserId = req.getParameter("one");
        //String so = req.getParameter("so");

        PushDevice myDevice = null;
        try {
            myDevice = (PushDevice) pm.getObjectById(idUser);
        } catch (Exception e) {
            myDevice = new PushDevice(idUser, DeviceType.ANDROID, token, oneSignalUserId);
            if (myDevice.getDeviceToken() == null) {//Its new no at city watch
                myDevice.setDeviceToken(token);
            }
        } finally {

            pm.makePersistent(myDevice);
            pm.close();

            js.put("id", myDevice.getKey().toString());
            js.put("token", myDevice.getDeviceToken());
            js.put("so", myDevice.getSo().toString());
            js.put("user", myDevice.getIdProfile());
///////
            return js;
        }
    }

    class PetList {

        Long idOwner;
        Long imagem;
        Long pet;

    }

    private static final List<Chat> getChatsByPetId(PersistenceManager pm, Long id) {
        String filter = "this.petKey==id";
        Query qc = pm.newQuery(Chat.class, filter);
        qc.declareParameters("Long id");
        return (List<Chat>) qc.execute(id);
    }

    public static final JSONObject getChatMessages(HttpServletRequest req, HttpServletResponse res) throws JSONException {
        pm = PMF.get().getPersistenceManager();
        String from = req.getParameter("from");
        String to = req.getParameter("to");
        String pet = req.getParameter("pet");

        List<Chat> lChats = getChatsByPetId(pm, new Long(pet));
        Collections.sort(lChats);

        Long idFrom, idTo;

        idFrom = new Long(from);
        idTo = new Long(to);

        JSONObject js = new JSONObject();
        JSONArray ja = new JSONArray();

        for (Chat c1 : lChats) {
            if (!((c1.getFrom().equals(idFrom) && c1.getTo().equals(idTo)) || (c1.getFrom().equals(idTo) && c1.getTo().equals(idFrom)))) {
                continue;
            }
            JSONObject js1 = new JSONObject();

            js1.put("getKey", c1.getKey().toString());
            js1.put("getEnabled", c1.getEnabled());
            js1.put("getFrom", c1.getFrom().toString());
            js1.put("getMsg", c1.getMsg());
            js1.put("getTit", c1.getTit());
            js1.put("getMsg", c1.getMsg());
            js1.put("getTo", c1.getTo().toString());
            js1.put("getTimestampChat", c1.getTimestampChat());
            try {
                Perfil pp = pm.getObjectById(Perfil.class, c1.getFrom());
                js1.put("getNome", pp.getNome());
                Imagem m = pm.getObjectById(Imagem.class, pp.getAvatar());

                String img = m.getImage().charAt(0) == '['
                        ? "https://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=5&blob-key=" + m.getImage().substring(2, +m.getImage().length() - 2)
                        : m.getImage();

                js1.put("getImage", img.replaceAll("http:", "https:"));
                js1.put("getPath", img.replaceAll("http:", "https:"));

            } catch (Exception e) {
                js1.put("getImage", "img/avatar.png");
                js1.put("getPath", "img/avatar.png");
            }

            ja.put(js1);
        }

        js.put("chats", ja);

        return js;

    }

    public static final JSONObject getChatAvatarToOwner(HttpServletRequest req, HttpServletResponse res) throws JSONException {
        JSONObject js = new JSONObject();
        Long idPet = new Long(req.getParameter("idPet"));
        pm = PMF.get().getPersistenceManager();
        HashMap<Long, Boolean> profiles = new HashMap<>();

        Pet p = pm.getObjectById(Pet.class, idPet);

        //Monta a lista de pets
        JSONArray ja = new JSONArray();
        List<Chat> lChats = new ArrayList<>();
        lChats.addAll(getChatsByPetId(pm, idPet));
        Collections.sort(lChats);
        js.put("lSize", lChats.size());
        for (Chat c : lChats) {

            JSONObject avatars = new JSONObject();
            if (!profiles.containsKey(c.getFrom())) {

                Perfil perfil = pm.getObjectById(Perfil.class, c.getFrom());
                //Não apresenta o proprio avatar ne maluco.....
                if (perfil.getKey().equals(p.getIdOwner())) {
                    continue;
                }

                avatars.put("getNome", perfil.getNome());
                avatars.put("getKey", perfil.getKey().toString());

                //try {
                Imagem m = pm.getObjectById(Imagem.class, perfil.getAvatar());

                String img = m.getImage().charAt(0) == '['
                        ? "https://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=5&blob-key=" + m.getImage().substring(2, +m.getImage().length() - 2)
                        : m.getImage();

                avatars.put("getImage", img);
                avatars.put("getPath", img);

                //avatars.put("getPath1", m.getImage());

                /*  } catch (Exception e) {
                 avatars.put("getImage", "img/avatar.png");
                 avatars.put("getPath", "img/avatar.png");*/
                //}
                profiles.put(perfil.getKey(), Boolean.TRUE);
                ja.put(avatars);
            }
        }
        js.put("result", ja);
        return js;
    }

    public static final JSONObject getPetMessages(HttpServletRequest req, HttpServletResponse res) throws JSONException {
        JSONObject js = new JSONObject();
        Long idProfile = new Long(req.getParameter("idProfile"));

        pm = PMF.get().getPersistenceManager();
        HashMap<Long, Perfil> mapaProf = new HashMap<>();
        HashMap<Long, List<JSONObject>> mapaChat = new HashMap<>();
        HashMap<Long, Imagem> mapaImagem = new HashMap<>();

        //Filter chat sent
        String filter = "this.to==id";
        Query qc = pm.newQuery(Chat.class, filter);
        qc.declareParameters("Long id");
        // HashMap<Long, Perfil> mp = new HashMap<>();
        //  HashMap<Long, Imagem> im = new HashMap<>();
        // HashMap<Long, Pet> lp = new HashMap<>();
        HashMap<Long, JSONArray> pets = new HashMap<>();

        //Monta a lista de pets
        JSONArray ja = new JSONArray();
        Set<Chat> lChats = new HashSet<>();
        lChats.addAll((List<Chat>) qc.execute(idProfile));

        filter = "this.idOwner==id";
        Query qf = pm.newQuery(Pet.class, filter);
        qf.declareParameters("Long id");
        List<Pet> myPets = (List<Pet>) qf.execute(idProfile);

        filter = "this.from==id";
        Query qc1 = pm.newQuery(Chat.class, filter);
        qc1.declareParameters("Long id");
        lChats.addAll((List<Chat>) qc1.execute(idProfile));

        for (Pet p : myPets) {
            JSONObject chat = new JSONObject();
            Imagem m = pm.getObjectById(Imagem.class, p.getAvatar());

            chat.put("mine", true);
            chat.put("getKey", p.getKey().toString());
            chat.put("getIdOwner", p.getIdOwner().toString());
            chat.put("getPath", m.getPath());
            chat.put("getTitulo", p.getTitulo());
            chat.put("getAdoptedBy", p.getAdoptedBy() == null ? false : true);
            //chat.put("", p.getKey());
            ja.put(chat);
            pets.put(p.getKey(), null);
        }

        for (Chat c : lChats) {
            JSONObject chat = new JSONObject();
            if (pets.containsKey(c.getPetKey())) {
                continue;
            }
            try {
                Pet p = pm.getObjectById(Pet.class, c.getPetKey());
                Imagem m = pm.getObjectById(Imagem.class, p.getAvatar());
                boolean mine = p.getIdOwner().equals(idProfile);

                chat.put("mine", mine);
                chat.put("getKey", p.getKey().toString());
                chat.put("getIdOwner", p.getIdOwner().toString());
                chat.put("getPath", m.getPath());
                chat.put("getTitulo", p.getTitulo());
                chat.put("getAdoptedBy", p.getAdoptedBy() == null ? false : true);
                //chat.put("", p.getKey());
                //chat.put("", p.getKey());
                ja.put(chat);

                pets.put(c.getPetKey(), null);
            } catch (Exception e) {
                continue;
            }
        }
        js.put("petMessages", ja);
        return js;
    }

    public static final JSONObject setAdotped(HttpServletRequest req, HttpServletResponse res) throws JSONException {

        //Request parameters
        Long idPet = new Long(req.getParameter("idPet"));
        Long idAdoptedBy = new Long(req.getParameter("idAdoptedBy"));

        //JS return
        JSONObject js = new JSONObject();

        //Connect to the datastore
        pm = PMF.get().getPersistenceManager();
        pm.currentTransaction().begin();

        Pet p = pm.getObjectById(Pet.class, idPet);

        p.setAdoptedBy(idAdoptedBy);

        pm.currentTransaction().commit();

        //Set parameters
        js.put("idPet", idPet);
        js.put("idAdoptedBy", idAdoptedBy);

        List<Chat> lChats = getChatsByPetId(pm, idPet);

        JSONArray ja = new JSONArray();

        String mensagem = p.getTitulo() + " foi adotado";
        Set<Long> keys = new HashSet<>();
        for (Chat c1 : lChats) {
            if (keys.contains(c1.getFrom())) {
                continue;
            }
            keys.add(c1.getFrom());
            try {
                PushDevice pd = pm.getObjectById(PushDevice.class, c1.getFrom());
                //PushDevice pd1 = pm.getObjectById(PushDevice.class, p.getIdOwner());

                ja.put(PushController.sendOneSignalPushToUser(pd.getOneSignalID(), "Adoção concluída", mensagem));
            } catch (Exception e) {
                Logger.getAnonymousLogger().log(Level.WARNING, e.getMessage());
            }
        }
        js.put("devices", ja);

        //Return to webservice
        return js;

    }
}
