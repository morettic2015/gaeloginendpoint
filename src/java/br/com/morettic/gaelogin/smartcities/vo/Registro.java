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
public class Registro implements Serializable ,Comparable<Registro>{

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;
    @Persistent(name = "dt_ocorrencia")
    private Date dtOcorrencia;
    @Persistent(name = "titulo")
    private String titulo;
    @Persistent(name = "descricao")
    private String descricao;
    @Persistent(name = "latitude")
    private Float latitude;
    @Persistent(name = "longitude")
    private Float longitude;
    @Persistent(name = "user_ip")
    private String ip;
    @Persistent(name = "adress")
    private String adress;
    @Persistent
    private Long perfilUsuario;
    @Persistent
    private TipoOcorrencia tipo;
    @Persistent(name = "avatar")
    private Long avatar;

    @Persistent(name = "avatar1")
    private Long avatar1;

    @Persistent(name = "avatar2")
    private Long avatar2;

    @Persistent(name = "avatar3")
    private Long avatar3;

    @Persistent
    private boolean enabledData;

    @Persistent
    private String segment;

    public Registro() {
        this.enabledData = true;
        this.dtOcorrencia = new Date();
        this.segment = "ALL";
    }

    public boolean isEnabledData() {
        return enabledData;
    }

    public void setEnabledData(boolean enabledData) {
        this.enabledData = enabledData;
    }

    public Long getKey() {
        return key;
    }

    public TipoOcorrencia getTipo() {
        return tipo;
    }

    public void setTipo(TipoOcorrencia tipo) {
        this.tipo = tipo;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public Date getDtOcorrencia() {
        return dtOcorrencia;
    }

    public void setDtOcorrencia(Date dtOcorrencia) {
        this.dtOcorrencia = dtOcorrencia;
    }

    public String getTitulo() {
        return titulo.toUpperCase();
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo.toUpperCase();
    }

    public String getDescricao() {
        return descricao.toUpperCase();
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao.toUpperCase();
    }

    public String getLatitude() {
        return latitude.toString();
    }

    public void setLatitude(String latitude) {
        this.latitude = new Float(latitude);
    }

    public String getLongitude() {
        return longitude.toString();
    }

    public void setLongitude(String longitude) {
        this.longitude = new Float(longitude);
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public Long getPerfil() {
        return perfilUsuario;
    }

    public void setPerfil(Long perfil) {
        this.perfilUsuario = perfil;
    }

    public Long getAvatar() {
        return avatar;
    }

    public void setAvatar(Long avatar) {
        this.avatar = avatar;
    }

    public Long getPerfilUsuario() {
        return perfilUsuario;
    }

    public void setPerfilUsuario(Long perfilUsuario) {
        this.perfilUsuario = perfilUsuario;
    }

    public Long getAvatar1() {
        return avatar1;
    }

    public void setAvatar1(Long avatar1) {
        this.avatar1 = avatar1;
    }

    public Long getAvatar2() {
        return avatar2;
    }

    public void setAvatar2(Long avatar2) {
        this.avatar2 = avatar2;
    }

    public Long getAvatar3() {
        return avatar3;
    }

    public void setAvatar3(Long avatar3) {
        this.avatar3 = avatar3;
    }

    public String getAdress() {
        return adress.toUpperCase();
    }

    public void setAdress(String adress) {
        this.adress = adress.toUpperCase();
    }

    @Override
    public String toString() {
        return "Ocorrencia{" + "key=" + key + ", dtOcorrencia=" + dtOcorrencia + ", titulo=" + titulo + ", descricao=" + descricao + ", latitude=" + latitude + ", longitude=" + longitude + ", ip=" + ip + ", cidade=" + adress + ", perfilUsuario=" + perfilUsuario + ", tipo=" + tipo + ", avatar=" + avatar + ", avatar1=" + avatar1 + ", avatar2=" + avatar2 + ", avatar3=" + avatar3 + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Registro other = (Registro) obj;
        if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
            return false;
        }
        if (this.dtOcorrencia != other.dtOcorrencia && (this.dtOcorrencia == null || !this.dtOcorrencia.equals(other.dtOcorrencia))) {
            return false;
        }
        if (this.tipo != other.tipo) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.key);
        hash = 59 * hash + Objects.hashCode(this.tipo);
        return hash;
    }

    @Override
    public int compareTo(Registro o) {
        return this.getTitulo().compareToIgnoreCase(o.getTitulo());
    }

}
