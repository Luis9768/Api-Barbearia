package com.barbearia.barbershop_api.dto;

import com.barbearia.barbershop_api.model.DiaEspecial;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

public record SaidaDiaEspecialDTO(
        LocalDate data,
        LocalTime horarioAbertura,
        LocalTime horarioFechamento,
        String descricao,
        String mensagem
) {
    // Construtor customizado que recebe a Entidade do banco
    public SaidaDiaEspecialDTO(DiaEspecial dia) {
        this(
                dia.getData(),

                // Se for folga, devolve null. Se não, devolve o horário de abertura.
                dia.getDiaFolga() ? null : dia.getHorarioAbertura(),

                // Se for folga, devolve null. Se não, devolve o horário de fechamento.
                dia.getDiaFolga() ? null : dia.getHorarioFechamento(),

                dia.getDescricao(),

                // A mágica da mensagem para o Front-end!
                dia.getDiaFolga() ? "É dia de folga!" : null
        );
    }
}
