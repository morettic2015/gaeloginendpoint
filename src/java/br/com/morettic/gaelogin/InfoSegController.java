/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin;

import br.com.morettic.gaelogin.smartcities.control.PerfilControler;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
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

        try {

            String action = request.getParameter("action");
            Integer actionNumber = -1;
            if (action != null) {
                actionNumber = new Integer(action);
            }

            switch (actionNumber) {
                case 1://Cadastra uma ocorrência;
                    retJSon = PerfilControler.saveOcorrencia(request, response);
                    break;
                case 2://Cadastra uma imagem;
                    retJSon = PerfilControler.saveImagem(request, response);
                    break;
                case 3://Cadastra um perfil;
                    retJSon = PerfilControler.savePerfil(request, response);
                    break;
                case 4:
                    retJSon = PerfilControler.findPerfilByIdOrEmail(request, response);
                    break;
                case 5:
                    response.setContentType("image/jpeg");
                    out = response.getWriter();
                    PerfilControler.showImageById(request, response);
                    break;
                case 6:
                    retJSon = PerfilControler.findOcorrencias(request, response);
                    break;
                case 7:
                    retJSon = PerfilControler.autenticaUsuario(request, response);
                    break;
                case 8:
                    out = response.getWriter();
                    PerfilControler.findImagemTokenById(request, response);
                    break;
                case 9:
                    retJSon = PerfilControler.getProfileFromLDAP(request,response);
                    break;
                case 10:
                    retJSon = PerfilControler.getWeatherInfoByLatLon(request,response);
                default:
                    ;
                    break;
            }

        } finally {
            if (retJSon != null) {
                out.print(retJSon);
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

}
