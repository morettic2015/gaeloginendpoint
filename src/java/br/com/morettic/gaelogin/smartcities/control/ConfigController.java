/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

import br.com.morettic.gaelogin.smartcities.vo.Configuracao;
import br.com.morettic.gaelogin.smartcities.vo.Perfil;
import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;
import java.util.HashMap;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author LuisAugusto
 */
public class ConfigController {

    private static PersistenceManager pm = null;

    /*
     http://gaeloginendpoint.appspot.com/infosegcontroller.exec?action=31
    &idProfile=5718080416120832
    &nrDoc=26712962
    &nasc=1979-04-29
    &cep=88015700
    &pais=Brazil
    &comp=Santa+Catarina
    &cidade=Florianopolis
    &bairro=beira+mar
    &push=on
    &cell=%2B554896004929
    &sex=MALE
    &rua=Avenida+das+torres
    &prop=BAR,BEER,CULTURA,EDUCACAO,ESPORTE,
     Atualiza endereço do perfil e dados da configuração
     */
    public static final JSONObject updateProfileConfig(HttpServletRequest request, JSONObject js1) throws JSONException {
        JSONObject js = js1;

        pm = PMF.get().getPersistenceManager();

        //Recover perfil
        Perfil p = pm.getObjectById(Perfil.class, Long.parseLong(request.getParameter("idProfile")));

        //Set perfil properties
        p.setCpfCnpj(request.getParameter("nrDoc"));
        p.setNascimento(request.getParameter("nasc"));
        p.setBairro(request.getParameter("bairro"));
        p.setCidade(request.getParameter("cidade"));
        p.setPais(request.getParameter("pais"));
        p.setCep(request.getParameter("cep"));
        p.setRua(request.getParameter("rua"));
        p.setComplemento(request.getParameter("comp"));
        p.setCpfCnpj(request.getParameter("nrDoc"));

        //Configuração
        Configuracao cfn;
        try {
            cfn = pm.getObjectById(Configuracao.class, Long.parseLong(request.getParameter("idProfile")));
        } catch (JDOObjectNotFoundException e) {
            cfn = new Configuracao();
            cfn.setOwner(p.getKey());
            cfn.setKey(p.getKey());
        }

        cfn.setCellPhone(request.getParameter("cell"));
        cfn.setPushEnabled(request.getParameter("push")==null ? false : request.getParameter("push").equals("")?false:true);

        //Mapa de configurações
        HashMap<String, String> prop = new HashMap<String, String>();

        //Set sexo
        prop.put("SEXO", request.getParameter("sex"));
        
        //set is a bussiness?
        String bussiness =  request.getParameter("bussiness")==null?"0":request.getParameter("bussiness").equals("1")?"IS_A_BUSSINESS":"NOT_A_BUSSINESS";
        prop.put("bussiness",bussiness);
        
        String[] lProperties = request.getParameter("props").split(",");
        JSONArray jaMyP = new JSONArray();
        //Mapa de properiedades
        for (String p1 : lProperties) {
            prop.put(p1, p1);
            jaMyP.put(p1);
        }

        //save properties
        cfn.setlPropriedades(prop);

        //Save perfil and config
        pm.makePersistent(p);
        pm.makePersistent(cfn);

        pm.close();

        /**
         * @Create JSON
         */
        js.put("push",cfn.getPushEnabled());
        js.put("exists", true);
        js.put("avatar", p.getAvatar());
        js.put("cep", p.getCep());
        js.put("complemento", p.getComplemento());
        js.put("cpfCnpj", p.getCpfCnpj());
        js.put("email", p.getEmail());
        js.put("key", p.getKey());
        js.put("nasc", p.getNascimento());
        js.put("nome", p.getNome());
        //js.put("pass", retorno.getPassWd());
        js.put("configId", p.getConfig());
        js.put("pjf", p.isEhPessoaFisica());
        js.put("cidade", p.getCidade());
        js.put("pais", p.getPais());
        js.put("bairro", p.getBairro());
        js.put("rua", p.getRua());
        js.put("cell", cfn.getCellPhone());
        js.put("myProps", jaMyP);

        return js;

    }

}
