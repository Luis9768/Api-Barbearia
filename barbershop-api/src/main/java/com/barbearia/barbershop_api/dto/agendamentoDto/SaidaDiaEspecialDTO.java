package com.barbearia.barbershop_api.dto.agendamentoDto;

import com.barbearia.barbershop_api.entity.DiaEspecial;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDate;
import java.time.LocalTime;
@JsonPropertyOrder({"descricao","data","horarioAbertura","horarioFechamento", "mensagem"})
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
