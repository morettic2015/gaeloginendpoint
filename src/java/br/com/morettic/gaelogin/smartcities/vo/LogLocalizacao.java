/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
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
public class LogLocalizacao implements Serializable, Comparable<LogLocalizacao>{
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long keyPerfil;

    @Persistent
    private String token;
    @Persistent
    private String myip;
    @Persistent
    private Date lastLog;
    @Persistent
    private Float lat;
    @Persistent
    private Float lon;

    public Float getLat() {
        return lat;
    }

    public void setLat(Float lat) {
        this.lat = lat;
    }

    public Float getLon() {
        return lon;
    }

    public void setLon(Float lon) {
        this.lon = lon;
    }

    public String getMyip() {
        return myip;
    }

    public void setMyip(String myip) {
        this.myip = myip;
    }
    
    
    public LogLocalizacao(String token,Long perfilId,Double lat,Double lon,String ip){
        this.lastLog = new Date();
        this.token = token;
        this.keyPerfil = perfilId;
        this.lat = Float.parseFloat(lat.toString());
        this.lon = Float.parseFloat(lon.toString());
        this.myip = ip;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.keyPerfil);
        hash = 17 * hash + Objects.hashCode(this.token);
        hash = 17 * hash + Objects.hashCode(this.lastLog);
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
        final LogLocalizacao other = (LogLocalizacao) obj;
        if (!Objects.equals(this.keyPerfil, other.keyPerfil)) {
            return false;
        }
        if (!Objects.equals(this.token, other.token)) {
            return false;
        }
        if (!Objects.equals(this.lastLog, other.lastLog)) {
            return false;
        }
        return true;
    }

    public Long getKeyPerfil() {
        return keyPerfil;
    }

    public void setKeyPerfil(Long keyPerfil) {
        this.keyPerfil = keyPerfil;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getLastLog() {
        return lastLog;
    }

    public void setLastLog(Date lastLog) {
        this.lastLog = lastLog;
    }

    @Override
    public int compareTo(LogLocalizacao o) {
        return (int) (lastLog.getTime()-o.getLastLog().getTime());
    }
    
}
