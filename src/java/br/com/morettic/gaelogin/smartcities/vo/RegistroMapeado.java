/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.vo;

import java.io.Serializable;
import javax.jdo.annotations.Cacheable;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 *
 * @author LuisAugusto
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
@Cacheable("true")
public class RegistroMapeado implements Serializable {

    public RegistroMapeado(Long pk,String geom,String geom_webmercator){
        this.key = pk;
        this.the_geom = geom;
        this.the_geom_webmercator = geom_webmercator;
    }
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    @Persistent(name = "the_geom")
    private String the_geom = null;

    @Persistent(name = "the_geom_webmercator")
    private String the_geom_webmercator = null;

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public String getThe_geom() {
        return the_geom;
    }

    public void setThe_geom(String the_geom) {
        this.the_geom = the_geom;
    }

    public String getThe_geom_webmercator() {
        return the_geom_webmercator;
    }

    public void setThe_geom_webmercator(String the_geom_webmercator) {
        this.the_geom_webmercator = the_geom_webmercator;
    }

}
