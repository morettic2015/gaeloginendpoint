/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function exibeInformacao(mensagem){
    alert(mensagem);
}

//JSON com a configuração
var configWindow = {
    largura: 1024,
    altura: 769,
    titulo: "Meu Jogo com o Crafty",
    gravidade: 0.5,
    lAvatar: "128px",
    aAvatar: "128px",
    exibeInformacao:function(mensagem){
        alert(mensagem);
    }
}

configWindow.exibeInformacao(configWindow.titulo);
configWindow.exibeInformacao(configWindow.largura);
configWindow.exibeInformacao(configWindow.altura);
configWindow.exibeInformacao(configWindow.gravidade);


