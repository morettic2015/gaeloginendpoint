/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.vo;

import java.io.Serializable;
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
public class PushDevice implements Serializable {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;
    @Persistent(name = "idProfile")
    private Long idProfile;
    @Persistent(name = "deviceToken")
    @Column(allowsNull = "true")
    private String deviceToken = null;

    @Persistent(name = "petToken")
    @Column(allowsNull = "true")
    private String petToken = null;

    @Persistent(name = "oneSignalID")
    @Column(allowsNull = "true")
    private String oneSignalID = null;

    @Persistent
    private DeviceType so;

    public DeviceType getSo() {
        return so;
    }

    public void setSo(DeviceType so) {
        this.so = so;
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final PushDevice other = (PushDevice) obj;
        if (!Objects.equals(this.deviceToken, other.deviceToken)) {
            return false;
        }
        if (this.so != other.so) {
            return false;
        }
        return true;
    }

    public String getOneSignalID() {
        return oneSignalID;
    }

    public void setOneSignalID(String oneSignalID) {
        this.oneSignalID = oneSignalID;
    }

    @Override
    public String toString() {
        return "PushDevice{" + "key=" + key + ", idProfile=" + idProfile + ", deviceToken=" + deviceToken + ", so=" + so + '}';
    }

    public PushDevice(Long idProfile, String deviceToken, DeviceType so) {
        this.idProfile = idProfile;
        this.deviceToken = deviceToken;
        this.so = so;
        this.key = idProfile;
    }

    public PushDevice(Long idProfile, DeviceType so, String pet, String oneSignalID) {
        this.idProfile = idProfile;
        //this.deviceToken = deviceToken;
        this.petToken = pet;
        this.so = so;
        this.key = idProfile;
        this.oneSignalID = oneSignalID;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public Long getIdProfile() {
        return idProfile;
    }

    public void setIdProfile(Long idProfile) {
        this.idProfile = idProfile;
    }

    public String getPetToken() {
        return petToken;
    }

    public void setPetToken(String petToken) {
        this.petToken = petToken;
    }

}
