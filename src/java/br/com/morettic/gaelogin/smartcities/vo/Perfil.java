/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.jdo.annotations.Cacheable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;

/**
 *
 * @author LuisAugusto
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
@Unique(
        name = "EMAIL_AK",
        members = {"email"}
)

@Cacheable("true")
public class Perfil implements Serializable {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;
    @Persistent(name = "email")
    @Column(allowsNull = "false")
    private String email;
    @Persistent(name = "nome")
    @Column(allowsNull = "false")
    private String nome;
    @Persistent(name = "nascimento")
    private String nascimento;
    @Persistent(name = "cpfCnpj")
    private String cpfCnpj;
    @Persistent(name = "cep")
    private String cep;
    @Persistent(name = "cidade")
    private String cidade;
    @Persistent(name = "rua")
    private String rua;
    @Persistent(name = "bairro")
    private String bairro;
    @Persistent(name = "pais")
    private String pais;
    @Persistent(name = "complemento")
    private String complemento;
    @Persistent(name = "avatar")
    private Long avatar;
    @Persistent(name = "passwd")
    @Column(allowsNull = "false")
    private String passWd;
    @Persistent(name = "origem")
    private String origem = "DEFAULT";
    @Persistent
    private Long config;
    @Persistent(name = "eh_pessoa_fisica")
    private String ehPessoaFisica;
    @Persistent(mappedBy = "perfilUsuario")
    private List<Long> lIDsOcorrencias = new ArrayList<Long>();

    public List<Long> getlOcorrencias() {
        return lIDsOcorrencias;
    }

    public Long getConfig() {
        return config;
    }

    public void setConfig(Long config) {
        this.config = config;
    }

    public void setlOcorrencias(List<Long> lOcorrencias) {
        this.lIDsOcorrencias = lOcorrencias;
    }

    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public String getNome() {
        return nome.toUpperCase();
    }

    public void setNome(String nome) {
        this.nome = nome.toUpperCase();
    }

    public String getEmail() {
        return email.toUpperCase();
    }

    public void setEmail(String email) {
        this.email = email.toUpperCase();
    }

    public String getNascimento() {
        return nascimento;
    }

    public void setNascimento(String nascimento) {
        this.nascimento = nascimento;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade.toUpperCase();
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public Long getAvatar() {
        return avatar;
    }

    public void setAvatar(Long avatar) {
        this.avatar = avatar;
    }

    public String isEhPessoaFisica() {
        return ehPessoaFisica;
    }

    public void setEhPessoaFisica(String ehPessoaFisica) {
        this.ehPessoaFisica = ehPessoaFisica;
    }

    public String getPassWd() {
        return passWd;
    }

    public void setPassWd(String passWd) {
        this.passWd = passWd;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.key != null ? this.key.hashCode() : 0);
        hash = 59 * hash + (this.email != null ? this.email.hashCode() : 0);
        hash = 59 * hash + (this.cpfCnpj != null ? this.cpfCnpj.hashCode() : 0);
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
        final Perfil other = (Perfil) obj;
        if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
            return false;
        }
        if ((this.email == null) ? (other.email != null) : !this.email.equals(other.email)) {
            return false;
        }
        if ((this.cpfCnpj == null) ? (other.cpfCnpj != null) : !this.cpfCnpj.equals(other.cpfCnpj)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Perfil{" + "key=" + key + ", email=" + email + ", nome=" + nome + ", nascimento=" + nascimento + ", cpfCnpj=" + cpfCnpj + ", cep=" + cep + ", cidade=" + cidade + ", rua=" + rua + ", bairro=" + bairro + ", pais=" + pais + ", complemento=" + complemento + ", avatar=" + avatar + ", passWd=" + passWd + ", config=" + config + ", ehPessoaFisica=" + ehPessoaFisica + ", lIDsOcorrencias=" + lIDsOcorrencias + '}';
    }

    public List<Long> getlIDsOcorrencias() {
        return lIDsOcorrencias;
    }

    public void setlIDsOcorrencias(List<Long> lIDsOcorrencias) {
        this.lIDsOcorrencias = lIDsOcorrencias;
    }

}
