/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

import br.com.morettic.gaelogin.smartcities.vo.Imagem;
import br.com.morettic.gaelogin.smartcities.vo.Perfil;
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
import java.util.List;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author LuisAugusto
 */
public class PetmatchController {

    private static PersistenceManager pm = null;
    private static BlobstoreService blobstoreService;
    private static final Double UMK = 0.1570d;
    private static UserService userService;
    private static User user;

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

            }
        } else {
            /**
             * Fazendo login ja e cadastrado
             */
            js.put("new", false);
            p1 = lRet.get(0);
            String passwd = req.getParameter("pass");
            if (passwd.equals(p1.getPassWd())) {
                js.put("in", true);
            } else if (id.equals(p1.getKey().toString())) {
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
}
