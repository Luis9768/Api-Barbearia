package com.barbearia.barbershop_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
public class DiaEspecial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "data", unique = true)
    private LocalDate data;

    @Column(name = "horario_abertura")
    private LocalTime horarioAbertura;

    @Column(name = "horario_fechamento")
    private LocalTime horarioFechamento;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "dia_folga")
    private Boolean diaFolga;

    public DiaEspecial(){}
}
