/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.vo;

import java.io.Serializable;
import java.util.Date;
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
public class RegistroAnonimo implements Serializable {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    @Persistent(name = "fakeEmail")
    private String fakeEmail;
    
    @Persistent(name="fakeName")
    private String fakeName;
    
    @Persistent(name = "fakeAvatar")
    private String fakeAvatar;
    
    @Persistent(name = "idFakeUserRegister")
    private Long fakeRegister;

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public String getFakeEmail() {
        return fakeEmail;
    }

    public void setFakeEmail(String fakeEmail) {
        this.fakeEmail = fakeEmail;
    }

    public String getFakeName() {
        return fakeName;
    }

    public void setFakeName(String fakeName) {
        this.fakeName = fakeName;
    }

    public String getFakeAvatar() {
        return fakeAvatar;
    }

    public void setFakeAvatar(String fakeAvatar) {
        this.fakeAvatar = fakeAvatar;
    }

    public Long getFakeRegister() {
        return fakeRegister;
    }

    public void setFakeRegister(Long fakeRegister) {
        this.fakeRegister = fakeRegister;
    }
    
    

}
