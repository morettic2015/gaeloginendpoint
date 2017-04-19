/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.vo;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 *
 * @author LuisAugusto
 */
@PersistenceCapable
@Inheritance(customStrategy = "complete-table")
public class Pet extends Registro {

    @Persistent(name = "especie")
    private EspeciePet especie;
    @Persistent(name = "idade")
    private Integer idade;
    @Persistent(name = "sexo")
    private Integer sexo;
    @Persistent(name = "porte")
    private Integer porte;
    @Persistent(name = "vacinado")
    private Integer vacinado;
    @Persistent(name = "castrado")
    private Integer castrado;

    public Pet() {
        this.setTipo(TipoOcorrencia.PET_MATCH);
    }

    public EspeciePet getEspecie() {
        return especie;
    }

    public void setEspecie(EspeciePet especie) {
        this.especie = especie;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public Integer getSexo() {
        return sexo;
    }

    public void setSexo(Integer sexo) {
        this.sexo = sexo;
    }

    public Integer getPorte() {
        return porte;
    }

    public void setPorte(Integer porte) {
        this.porte = porte;
    }

    public Integer getVacinado() {
        return vacinado;
    }

    public void setVacinado(Integer vacinado) {
        this.vacinado = vacinado;
    }

    public Integer getCastrado() {
        return castrado;
    }

    public void setCastrado(Integer castrado) {
        this.castrado = castrado;
    }

}
