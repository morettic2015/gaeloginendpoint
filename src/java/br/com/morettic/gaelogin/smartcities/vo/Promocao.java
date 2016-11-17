/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
public class Promocao implements Serializable, Comparable<Promocao> {

    /**
     * @param tit String title
     * @param msg String message
     * @param from Long id profile
     * @param canais TipoOcorrencia lista de canais para publicacao
     * @ COnstrutor da promoção
     */
    public Promocao(String tit, String msg, Long from, List<TipoOcorrencia> canais) {
        this.tit = tit;
        this.msg = msg;
        this.from = from;
        this.lTipos = new ArrayList<String>();
        for (TipoOcorrencia tp : canais) {
            lTipos.add(tp.name());
        }
        this.timestampPromo = new Date();
        this.enabled = true;
    }

    public List<Long> getlDestinatarios() {
        return lDestinatarios;
    }

    public void setlDestinatarios(List<Long> lDestinatarios) {
        this.lDestinatarios = lDestinatarios;
    }

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    @Column(allowsNull = "false", name = "title")
    @Persistent
    private String tit;

    @Column(allowsNull = "false", name = "promotion")
    @Persistent
    private String msg;

    @Column(allowsNull = "false", name = "from_perfil")
    @Persistent
    private Long from;

    @Persistent(serialized = "true", defaultFetchGroup = "true")
    List<String> lTipos = new ArrayList<String>();

    @Persistent(serialized = "true", defaultFetchGroup = "true")
    List<Long> lDestinatarios = new ArrayList<Long>();

    @Column(allowsNull = "false", name = "time_promotion")
    @Persistent
    private Date timestampPromo;

    @Column(allowsNull = "false", name = "enabled")
    @Persistent
    private Boolean enabled;

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

    @Override
    public int compareTo(Promocao o) {
        return this.timestampPromo.toString().compareTo(o.getTimestampPromo().toString());
    }

    public List<String> getlTipos() {
        return lTipos;
    }

    public void setlTipos(ArrayList<String> lTipos) {
        this.lTipos = lTipos;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.key);
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
        final Promocao other = (Promocao) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "{" + "key:" + key + ", tit:'" + tit + "', msg:'" + msg + "', from:" + from + ", lTipos:'" + lTipos + "', timestampChat:'" + timestampPromo + "'}";
    }

    public Date getTimestampPromo() {
        return timestampPromo;
    }

    public void setTimestampPromo(Date timestampPromo) {
        this.timestampPromo = timestampPromo;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

}
