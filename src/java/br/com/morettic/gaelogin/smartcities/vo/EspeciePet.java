/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.vo;

/**
 *
 * @author LuisAugusto
 */
public enum EspeciePet {

    CAO(1),
    GATO(2);

    private final int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        id = id;
    }

    EspeciePet(int idp) {
        id = idp;
    }

}
