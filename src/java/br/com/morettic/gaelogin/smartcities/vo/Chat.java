/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.vo;

import com.google.appengine.api.datastore.Blob;
import java.io.Serializable;
import java.util.Date;
import javax.jdo.annotations.Cacheable;
import javax.jdo.annotations.Column;
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
public class Chat implements Serializable, Comparable<Chat> {
    
    public Chat(String t,String m, String fo, String to){
        this.tit = t;
        this.msg = m;
        this.from = Long.parseLong(fo);
        this.to = Long.parseLong(to);
        this.timestampChat = new Date();
        enabled = true;
    }
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    @Column(allowsNull = "false", name = "title")
    @Persistent
    private String tit;

    @Column(allowsNull = "false", name = "enabled")
    @Persistent
    private Boolean enabled;

    @Column(allowsNull = "false", name = "message")
    @Persistent
    private String msg;

    @Column(allowsNull = "false", name = "from_perfil")
    @Persistent
    private Long from;

    @Column(allowsNull = "false", name = "to_perfil")
    @Persistent
    private Long to;

    @Column(allowsNull = "false", name = "time_chat")
    @Persistent
    private Date timestampChat;

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public String getTit() {
        return tit.toUpperCase();
    }

    public void setTit(String tit) {
        this.tit = tit.toUpperCase();
    }

    public String getMsg() {
        return msg.toUpperCase();
    }

    public void setMsg(String msg) {
        this.msg = msg.toUpperCase();
    }

  
    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    public Date getTimestampChat() {
        return timestampChat;
    }

    public void setTimestampChat(Date timestampChat) {
        this.timestampChat = timestampChat;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int compareTo(Chat o) {
        return this.timestampChat.toString().compareTo(o.getTimestampChat().toString());
    }

}
