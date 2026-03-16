package com.barbearia.barbershop_api.dto;

import com.barbearia.barbershop_api.model.Agendamento;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record DadosSaidaAgendamento(
        String nome,
        String nomeServico,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime dataHoraInicio,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime dataHoraFim,
        String nomeBarbeiro
) {
    public DadosSaidaAgendamento(Agendamento agendamento) {
        this(
                agendamento.getCliente().getNome(),
                agendamento.getServico().getNome(),
                agendamento.getDataHoraInicio(),
                agendamento.getDataHoraFim(),
                agendamento.getBarbeiro().getNome()
        );
    }
}
