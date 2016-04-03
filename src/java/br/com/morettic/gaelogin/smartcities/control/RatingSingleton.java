/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.control;

import br.com.morettic.gaelogin.smartcities.vo.Rating;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.persistence.Persistence;

/**
 *
 * @author LuisAugusto
 */
public class RatingSingleton {
    private static HashMap<Long,HashMap<Long,Double>> lRatings;
    
    
    public static final boolean isEmpty(){
        return lRatings.isEmpty();
    }
    
    static{
        lRatings = new HashMap<>();
    }
    
    
    public static final void put(Long idOcorrencia, Long idRating, Double r){
        if(lRatings.containsKey(idOcorrencia)){
            HashMap<Long,Double> lVotos = lRatings.get(idOcorrencia);
            if(!lVotos.containsKey(idRating)){
                lVotos.put(idRating, r);
                lRatings.put(idOcorrencia, lVotos);
            }
        }else{
            HashMap<Long,Double> lVotos = new HashMap<>();
            lVotos.put(idRating, r);
            lRatings.put(idOcorrencia, lVotos);
        }
    }
    
    public static final void init(PersistenceManager pm){
        if (RatingSingleton.isEmpty()) {
            Query q1 = pm.newQuery(Rating.class);
            List<Rating> ratingBarList = (List<Rating>) q1.execute();
            for (Rating r : ratingBarList) {
                RatingSingleton.put(r.getIdOcorrencia(), r.getKey(), r.getRating());
            }
        }
    }
    
    public static final Double getRating(Long idOcorrencia){
        if(lRatings.containsKey(idOcorrencia)){
            Double total = 0.0d;
            HashMap<Long,Double> lVotos = lRatings.get(idOcorrencia);
            Set<Long>lKeys = lVotos.keySet();
            for(Long idRating:lKeys){
                total+=lVotos.get(idRating);
            }
            return total/lKeys.size();
        }else{
            return 0d;
        }
    }
}


