package com.barbearia.barbershop_api.infra;

import lombok.Data;

@Data
public class MensagemErro {
    private String mensagem;
    private Integer status;

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
