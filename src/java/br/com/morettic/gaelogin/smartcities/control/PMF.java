/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

/**
 *
 * @author LuisAugusto
 */
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public final class PMF {

    private static final PersistenceManagerFactory pmfInstance
            = JDOHelper.getPersistenceManagerFactory("transactions-optional");
   /* private static final EntityManagerFactory emfInstance
            = Persistence.createEntityManagerFactory("transactions-optional");*/


   /* public static EntityManagerFactory getEntityManagerFactory() {
        return emfInstance;
    }*/

    private PMF() {
    }

    public static PersistenceManagerFactory get() {
        return pmfInstance;
    }
    
  /*  public static  EntityManager getEntityManager(){
        return emfInstance.createEntityManager();
    }*/
}
