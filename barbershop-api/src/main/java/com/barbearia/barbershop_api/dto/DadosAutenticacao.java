package com.barbearia.barbershop_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

public record DadosAutenticacao(String login, String senha) {
}
