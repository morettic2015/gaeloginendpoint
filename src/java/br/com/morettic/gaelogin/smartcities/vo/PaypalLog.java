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
class PaypalLog implements Serializable {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    @Column(allowsNull = "false", name = "log_info")
    @Persistent
    private String log;

    @Column(allowsNull = "false", name = "log_timestamp")
    @Persistent
    private Date log_timestamp;

    @Persistent
    private MeuPlano plano;

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Date getLog_timestamp() {
        return log_timestamp;
    }

    public void setLog_timestamp(Date log_timestamp) {
        this.log_timestamp = log_timestamp;
    }

    public MeuPlano getPlano() {
        return plano;
    }

    public void setPlano(MeuPlano plano) {
        this.plano = plano;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.key);
        hash = 41 * hash + Objects.hashCode(this.log);
        hash = 41 * hash + Objects.hashCode(this.log_timestamp);
        hash = 41 * hash + Objects.hashCode(this.plano);
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
        final PaypalLog other = (PaypalLog) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        if (!Objects.equals(this.log, other.log)) {
            return false;
        }
        if (!Objects.equals(this.log_timestamp, other.log_timestamp)) {
            return false;
        }
        if (!Objects.equals(this.plano, other.plano)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "{" + "key:" + key + ", log:" + log + ", log_timestamp:" + log_timestamp + ", plano:" + plano + '}';
    }

}
