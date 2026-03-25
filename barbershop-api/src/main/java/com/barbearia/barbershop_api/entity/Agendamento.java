package com.barbearia.barbershop_api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Data
@SQLDelete(sql = "UPDATE agendamento SET ativo = false WHERE id = ?")
@SQLRestriction("ativo = true")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;

    @ManyToOne
    @Getter
    @JoinColumn(name = "cliente_id", referencedColumnName = "id", columnDefinition = "INT")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "servico_id", referencedColumnName = "id", columnDefinition = "INT")
    private Servico servico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbeiro_id")
    private Barbeiro barbeiro;

    private Boolean ativo = true;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusAgendamento statusAgendamento;

    @PrePersist
    public void prePersist() {
       if(this.ativo == null){
           this.ativo = true;
       }
    }

    public Agendamento() {

    }
}
