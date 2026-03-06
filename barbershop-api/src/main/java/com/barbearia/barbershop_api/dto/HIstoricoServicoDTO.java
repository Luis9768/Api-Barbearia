package com.barbearia.barbershop_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record HIstoricoServicoDTO(
        String nomeAutor,
        LocalDateTime dataAlteracao,
        String tipoAlteracao,
        String nomeServico,
        Double preco,
        Boolean ativo
) {
}
