package com.barbearia.barbershop_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DadosEntradaCadastroAgendamento(
        @NotNull(message = "A data e hora são obrigatórias!")
        @Future(message = "A data do agendamento deve ser no futuro!")
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime dataHoraInicio,

        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime dataHoraFim,// Opcional: geralmente o sistema calcula isso baseado na duração do serviço

        @NotNull(message = "O ID do cliente é obrigatório!")
        Integer clienteId,

        @NotNull(message = "O ID do serviço é obrigatório!")
        Integer servicoId) {
}
