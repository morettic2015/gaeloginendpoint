/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin;

import br.com.morettic.gaelogin.smartcities.control.AirbnbController;
import br.com.morettic.gaelogin.smartcities.control.ConfigController;
import br.com.morettic.gaelogin.smartcities.control.ManguevivoController;
import br.com.morettic.gaelogin.smartcities.control.PerfilController;
import br.com.morettic.gaelogin.smartcities.control.PetmatchController;
import br.com.morettic.gaelogin.smartcities.control.PushController;
import br.com.morettic.gaelogin.smartcities.vo.PetmatchAction;
import br.com.morettic.gaelogin.smartcities.vo.TipoOcorrencia;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author LuisAugusto
 */
public class InfoSegController extends HttpServlet {

    public static final Logger log = Logger.getLogger(InfoSegController.class.getName());

    private JSONObject retJSon;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, JSONException {
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();
        PetmatchAction pma = PetmatchAction.SIGNIN;
        int responseType = 0;
        try {
            JSONObject js = new JSONObject();
            String action = request.getParameter("action");
            Integer actionNumber = -1;
            if (action != null) {
                actionNumber = new Integer(action);
            }
            out = response.getWriter();
            switch (actionNumber) {
                case 1://Cadastra uma ocorrência;
                    retJSon = PerfilController.saveOcorrencia(request, response);
                    break;
                case 2://Cadastra uma imagem;
                    retJSon = PerfilController.saveImagem(request, response);
                    break;
                case 3://Cadastra um perfil;
                    retJSon = PerfilController.savePerfil(request, response);
                    break;
                case 4:
                    retJSon = PerfilController.findPerfilByIdOrEmail(request, response);
                    break;
                case 5:
                    response.setContentType("image/jpeg");
                    PerfilController.showImageById(request, response);
                    break;
                case 6:
                    retJSon = PerfilController.findOcorrencias(request, response);
                    break;
                case 7:
                    retJSon = PerfilController.autenticaUsuario(request, response);
                    break;
                case 8:
                    PerfilController.findImagemTokenById(request, response);
                    break;
                case 9:
                    retJSon = PerfilController.getProfileFromLDAP(request, response);
                    break;
                case 10:
                    retJSon = PerfilController.getWeatherInfoByLatLon(request, response);
                    break;
                case 11:
                    retJSon = PerfilController.hasEmailIntoDataStore(request, response);
                    break;
                case 12:
                    retJSon = PerfilController.sendGMail(request, response);
                    break;
                case 13:
                    retJSon = PerfilController.ocorrenciaRating(request, response);
                    break;
                case 14:
                    retJSon = PerfilController.getProfileFromDeepWeb(request, response);
                    break;
                case 15:
                    retJSon = PerfilController.updateConfigInfoFromProfile(request, response);
                    break;
                case 16:
                    retJSon = PerfilController.findListOcorrenciasRecentes(request, response);
                    break;
                case 17:
                    retJSon = PerfilController.initDataStoreInfo(request, response);
                    break;
                case 18:
                    retJSon = PerfilController.loadNuclearPowerPlants(request, response);
                    break;
                case 19:
                    retJSon = PushController.registerUserDevice(request, response);
                    break;
                case 20:
                    retJSon = PushController.getRegisteredDevices(request);
                    break;
                case 21:
                    response.setContentType("application/json; charset=ISO-8859-1");
                    retJSon = PerfilController.findOpenStreeMapInfo(request, response);
                    break;
                case 22:
                    retJSon = PushController.sendMessage(request);
                    break;
                case 23:
                    retJSon = PushController.getMessages(request);
                    break;
                case 24:
                    retJSon = PushController.sendPromo(request);
                    break;
                case 25:
                    retJSon = PushController.getPromos(request);
                    break;
                case 26:
                    retJSon = PushController.createContact(request);
                    break;
                case 27:
                    retJSon = PerfilController.getMyFavorites(request);
                    break;
                case 29:
                    retJSon = PerfilController.getMyWebsiteProfile(request, getMyTypes());
                    break;
                case 28://RESULT MY TYPES FROM ENUM
                    retJSon = getMyTypes();
                    break;
                case 30:

                    retJSon = PerfilController.perfilExists(request, getMyTypes());
                    break;
                case 31:
                    retJSon = ConfigController.updateProfileConfig(request, getMyTypes());
                    break;
                case 32:
                    retJSon = PerfilController.getMyExperiences(request);
                    break;
                case 33:
                    retJSon = PushController.sendPushResumeFromLocation(request);
                    break;
                case 34:
                    retJSon = PerfilController.joinUs(request);
                    break;
                case 35://AIRBNB TEST ONLY
                    AirbnbController airbnbController = new AirbnbController(request.getParameter("city"), Double.parseDouble(request.getParameter("lat")), Double.parseDouble(request.getParameter("lon")));
                    retJSon = js.put("result", airbnbController.doSearch());
                    break;
                case 36://MANGUE VIVO
                    ManguevivoController mvController = new ManguevivoController();
                    retJSon = js.put("result", mvController.doSearch());
                    break;
                /**
                 * @petmatchController with JSONP
                 */
                case 37://Login facebook normal
                    retJSon = PetmatchController.loginProfile(request, response);
                    responseType = 1;
                    pma = PetmatchAction.SIGNIN;
                    break;
                case 38://Update profile
                    retJSon = PetmatchController.updateProfile(request, response);
                    responseType = 1;
                    pma = PetmatchAction.UPDATE_PROFILE;
                    break;
                case 39://register user devoxe
                    retJSon = PetmatchController.registerUserDevice(request, response);
                    responseType = 1;
                    pma = PetmatchAction.PUSH_REGISTER;
                    break;
                case 40://register user devoxe
                    retJSon = PerfilController.getUploadPath(request, response);
                    responseType = 1;
                    pma = PetmatchAction.UPLOAD_PATH;
                    break;
                case 41://register user devoxe
                    retJSon = PetmatchController.saveUpdatePet(request, response);
                    responseType = 1;
                    pma = PetmatchAction.UPDATE_PET;
                    break;
                case 42://register user devoxe
                    retJSon = PetmatchController.removePet(request, response);
                    responseType = 1;
                    pma = PetmatchAction.REMOVE;
                    break;
                case 43://register user devoxe
                    retJSon = PetmatchController.findPetsNearBy(request, response);
                    responseType = 1;
                    pma = PetmatchAction.SEARCH;
                    break;
                case 44://register user devoxe
                    retJSon = PetmatchController.getMyPets(request, response);
                    responseType = 1;
                    pma = PetmatchAction.MINE;
                    break;
                case 45://register user devoxe
                    retJSon = PetmatchController.saveChat(request, response);
                    responseType = 1;
                    pma = PetmatchAction.CHAT;
                    break;
                case 46://register user devoxe
                    retJSon = PetmatchController.getPetMessages(request, response);
                    responseType = 1;
                    pma = PetmatchAction.CHAT_LIST;
                    break;
                case 47://register user devoxe
                    retJSon = PetmatchController.getChatAvatarToOwner(request, response);
                    responseType = 1;
                    pma = PetmatchAction.CHAT_AVATAR;
                    break;
                case 48://register user devoxe
                    retJSon = PetmatchController.getChatMessages(request, response);
                    responseType = 1;
                    pma = PetmatchAction.CHAT_MSG;
                    break;

                case 49://register user devoxe
                    retJSon = PetmatchController.setAdotped(request, response);
                    responseType = 1;
                    pma = PetmatchAction.ADOPT_BY;
                    break;
                /* case 99:
                 //js.put("wList", URLReader.getWebhoseIoResults("Florianopolis"));
                 retJSon =
                 break;*/
                default://LOGA NO GOOGLE E CRIA UM USUARIO MAN
                    response.setContentType("text/html; charset=UTF-8");
                    PerfilController.autenticaUsuarioGoogle(request, response);
                    break;
            }

        } catch (Exception e) {
            //e.printStackTrace();
            Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage());
            Logger.getAnonymousLogger().log(Level.SEVERE, e.toString());
        } finally {
            if (retJSon != null) {
                //log.info(retJSon.toString());
                if (responseType == 0) {
                    out.print(retJSon);
                } else {
                    response.setContentType("text/javascript");
                    out.print(pma.toString() + "(" + retJSon.toString() + ")");
                }
            }

            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (JSONException ex) {
            Logger.getLogger(InfoSegController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (JSONException ex) {
            Logger.getLogger(InfoSegController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    public static final JSONObject getMyTypes() throws JSONException {
        JSONArray ja = new JSONArray();

        List<String> lStrings = new ArrayList<String>();
        for (TipoOcorrencia tp : TipoOcorrencia.values()) {
            if (tp.isIsVisible()) {
                lStrings.add(tp.name());
            }
        }

        Collections.sort(lStrings);

        for (String t : lStrings) {
            ja.put(t);
        }

        lStrings.clear();

        JSONObject js = new JSONObject();
        js.put("types", ja);

        return js;
    }
}
