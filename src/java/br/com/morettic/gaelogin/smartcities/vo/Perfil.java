/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Key;
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
        name="EMAIL_AK", 
        members={"email"}
)
public class Perfil {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long key;

    @Persistent(name = "email")
    private String email;
    @Persistent(name = "nome")
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
    private String passWd;

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
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
        this.cidade = cidade;
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

}
