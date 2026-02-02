package com.barbearia.barbershop_api.dto;

import java.math.BigDecimal;

public class FaturamentoDTO {
    private String mensagem;
    private BigDecimal valor;

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
