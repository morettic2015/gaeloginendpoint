/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.jdo.annotations.Cacheable;
import javax.jdo.annotations.Column;
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
public class Contato implements Serializable {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    /**
     * @ Hashmap LOng = Perfil pk e String = Description do contato
     */
    @Persistent(serialized = "true", defaultFetchGroup = "true")
    private List<Long> lPropriedades = new ArrayList<>();

    @Column(allowsNull = "false", name = "perfil_owner")
    @Persistent
    private Long perfil;

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public List<Long> getlPropriedades() {
        return lPropriedades;
    }

    public void setlPropriedades(List<Long> lPropriedades) {
        this.lPropriedades = lPropriedades;
    }

    public Long getPerfil() {
        return perfil;
    }

    public void setPerfil(Long perfil) {
        this.perfil = perfil;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.key);
        hash = 37 * hash + Objects.hashCode(this.perfil);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Contato other = (Contato) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        if (!Objects.equals(this.perfil, other.perfil)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Contato{" + "key=" + key + ", lPropriedades=" + lPropriedades + ", perfil=" + perfil + '}';
    }

}
