/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.morettic.gaelogin.smartcities.vo;

/**
 * @author LuisAugusto
 */
public enum TipoOcorrencia {

    SERVICOS(false),
    SAUDE(true),
    POLITICA(true),
    MEIO_AMBIENTE(true),
    EDUCACAO(true),
    TRANSPORTE(true),
    OUTROS(false),
    POSTO_SAUDE(false),
    UPA(false),
    ESPORTE(true),
    TURISMO(true),
    TEMPO(true),
    HOTEL(false),
    IMOVEIS(true),
    USINA_NUCLEAR(false),
    SEGURANCA(true),
    ALIMENTACAO(true),
    SHOP(true),
    CULTURA(true),
    BAR(false),
    RESTAURANTE(false),
    BEER(true),
    CINEMA(false),
    TEATRO(false),
    FETAESC(true),
    MANGUE_VIVO(true),
    INFRAESTRUTURA(true);

    private TipoOcorrencia(Boolean visible) {
        this.isVisible = visible;
    }

    public boolean isIsVisible() {
        return isVisible;
    }

    public void setIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    private boolean isVisible = false;
}
