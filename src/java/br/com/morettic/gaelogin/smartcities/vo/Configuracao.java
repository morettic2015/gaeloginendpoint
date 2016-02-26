/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.vo;

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
public class Configuracao {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;
    @Persistent(name = "only_mine")
    private boolean onlyMine;

    @Persistent(name = "only_state")
    private boolean onlyState;

    @Persistent(name = "only_city")
    private boolean onlyCity;

    @Persistent(name = "only_country")
    private boolean onlyCountry;

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public boolean isOnlyMine() {
        return onlyMine;
    }

    public void setOnlyMine(boolean onlyMine) {
        this.onlyMine = onlyMine;
    }

    public boolean isOnlyState() {
        return onlyState;
    }

    public void setOnlyState(boolean onlyState) {
        this.onlyState = onlyState;
    }

    public boolean isOnlyCity() {
        return onlyCity;
    }

    public void setOnlyCity(boolean onlyCity) {
        this.onlyCity = onlyCity;
    }

    public boolean isOnlyCountry() {
        return onlyCountry;
    }

    public void setOnlyCountry(boolean onlyCountry) {
        this.onlyCountry = onlyCountry;
    }

}
