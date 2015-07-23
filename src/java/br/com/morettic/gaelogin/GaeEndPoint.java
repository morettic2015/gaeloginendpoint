/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
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
public class GaeEndPoint extends HttpServlet {

    private UserService userService;
    private User user;
    //private Person myself;

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

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
            throws ServletException, IOException {
        //response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            userService = UserServiceFactory.getUserService();
            user = userService.getCurrentUser();
            if (user == null) {
                response.sendRedirect(getUserService().createLoginURL(request.getRequestURI()));
            } else {

               // out.print(request.getAttribute(CALLBACK));

                StringBuilder sb = new StringBuilder();
              //  sb.append("");
             //   sb.append("/");
                sb.append(URLPATH);

                sb.append(user.getEmail());
                sb.append("/");
                sb.append(user.getNickname());
                sb.append("/");
                sb.append("nonono");

              //  out.print(request.getAttribute(sb.toString()));
                out.print("loading...");
                out.print("<meta http-equiv=\"refresh\" content=\"0;url="+sb.toString()+"\">");

                //response.sendRedirect(sb.toString());
            }
        } finally {
            out.close();
        }
    }
    public static final String CALLBACK = "callbackURL";
    public static final String FI = "FI";
    public static final String NICK = "nick";
    public static final String EMAIL = "email";
    public static final String URLPATH = "http://localhost:8080/smartcities/rest/profiles/google/";

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
        processRequest(request, response);
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
        processRequest(request, response);
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
