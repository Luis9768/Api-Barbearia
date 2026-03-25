package com.barbearia.barbershop_api.dto.agendamentoDto;

import com.barbearia.barbershop_api.entity.Agendamento;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record DadosSaidaAgendamento(
        Integer id,
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
                agendamento.getId(),
                agendamento.getCliente().getNome(),
                agendamento.getServico().getNome(),
                agendamento.getDataHoraInicio(),
                agendamento.getDataHoraFim(),
                agendamento.getBarbeiro().getNome()
        );
    }
}
