/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.vo;

import java.io.Serializable;
import java.util.HashMap;
import javax.jdo.annotations.Cacheable;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 *
 * @author LuisAugusto
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
@Cacheable("true")
public class Configuracao implements Serializable {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    @Persistent
    private Long owner;

    @Persistent
    private Boolean pushEnabled = true;

    @Persistent(cacheable = "true")
    private String cellPhone;

    @Persistent(serialized = "true", defaultFetchGroup = "true")
    private HashMap<String, String> lPropriedades;

    public Configuracao() {
        this.lPropriedades = new HashMap<String, String>();
    }

    public String getValue(String key) {
        return this.lPropriedades.get(key);
    }

    public Long getOwner() {
        return owner;
    }

    public void setOwner(Long owner) {
        this.owner = owner;
    }

    public boolean getPushEnabled() {
        return pushEnabled;
    }

    public void setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public void setPairValue(String key, String Value) {
        if (this.lPropriedades == null) {
            this.lPropriedades = new HashMap();
        }
        this.lPropriedades.put(key, Value);
    }

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public HashMap<String, String> getlPropriedades() {
        return lPropriedades;
    }

    public void setlPropriedades(HashMap<String, String> lPropriedades) {
        this.lPropriedades = lPropriedades;
    }

}
