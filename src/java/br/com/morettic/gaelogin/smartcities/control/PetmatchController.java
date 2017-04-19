/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

import static br.com.morettic.gaelogin.smartcities.control.PerfilController.HTTPSVIACEPCOMBRWS;
import static br.com.morettic.gaelogin.smartcities.control.URLReader.readJSONUrl;
import br.com.morettic.gaelogin.smartcities.vo.Configuracao;
import br.com.morettic.gaelogin.smartcities.vo.DeviceType;
import br.com.morettic.gaelogin.smartcities.vo.Imagem;
import br.com.morettic.gaelogin.smartcities.vo.Perfil;
import br.com.morettic.gaelogin.smartcities.vo.Pet;
import br.com.morettic.gaelogin.smartcities.vo.PushDevice;
import br.com.morettic.gaelogin.smartcities.vo.TipoOcorrencia;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

                URL url = new URL(imgPath);
                InputStream input = url.openStream();
                byte[] imageData = getBytesFromInputStream(input);

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
                js.put("id", p1.getKey());

            }
        } else {
            /**
             * Fazendo login ja e cadastrado
             */

            js.put("new", false);
            p1 = lRet.get(0);
            js.put("name", p1.getNome());
            js.put("email", p1.getEmail());
            js.put("id", p1.getKey());
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
        pm.close();
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

    public static final JSONObject savePet(HttpServletRequest req, HttpServletResponse res) throws JSONException {
        JSONObject js = new JSONObject();
        pm = PMF.get().getPersistenceManager();

        Pet p = new Pet();
        p.setAdress("");
        p.setEnabledData(true);
        p.setCastrado(1);
        p.setIdade(11);
        p.setLatitude("123");
        p.setLongitude("123");
        p.setDtOcorrencia(new Date());
        p.setAvatar(1l);
        p.setTitulo("TIT");
        p.setDescricao("DESC");
        p.setIp("123123123");
        p.setAdress("123");
        p.setPerfil(1l);
        p.setTipo(TipoOcorrencia.PET_MATCH);

        pm.makePersistent(p);

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

            js.put("id", myDevice.getKey());
            js.put("token", myDevice.getDeviceToken());
            js.put("so", myDevice.getSo().toString());
            js.put("user", myDevice.getIdProfile());
///////
            return js;
        }
    }
}
