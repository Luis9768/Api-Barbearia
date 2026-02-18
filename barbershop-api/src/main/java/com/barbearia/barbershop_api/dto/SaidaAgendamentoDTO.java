package com.barbearia.barbershop_api.dto;

import com.barbearia.barbershop_api.model.Agendamento;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@JsonPropertyOrder({"clienteId", "servicoId", "dataHoraInicio", "dataHoraFim"})
public class SaidaAgendamentoDTO {

    private Integer id;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    @Schema(type = "string", example = "24/12/2026 14:30")
    private LocalDateTime dataHoraInicio;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    @Schema(type = "string", example = "24/12/2026 15:30")
    private LocalDateTime dataHoraFim;

    private Integer clienteId;
    private Integer servicoId;

    public SaidaAgendamentoDTO() {}

    // Mantemos esse construtor, ele é utilissimo para converter o que vem do banco!
    public SaidaAgendamentoDTO(Agendamento agendamento) {
        this.id = agendamento.getId();
        this.dataHoraInicio = agendamento.getDataHoraInicio();
        this.dataHoraFim = agendamento.getDataHoraFim();
        // Cuidado com NullPointerException aqui se cliente ou serviço forem nulos!
        if (agendamento.getCliente() != null) {
            this.clienteId = agendamento.getCliente().getId();
        }
        if (agendamento.getServico() != null) {
            this.servicoId = agendamento.getServico().getId();
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    public void setDataHoraFim(LocalDateTime dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public Integer getServicoId() {
        return servicoId;
    }

    public void setServicoId(Integer servicoId) {
        this.servicoId = servicoId;
    }
}
