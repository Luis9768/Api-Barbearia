package com.barbearia.barbershop_api.model;

import com.barbearia.barbershop_api.dto.ClienteDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Table(name = "clientes")
@SQLRestriction("ativo = true")
@SQLDelete(sql = "UPDATE clientes SET ativo = false WHERE id = ?")
public class Cliente {
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //getters e setters
    @Setter
    @Getter
    @Column(nullable = false)
    private String nome;

    @Setter
    @Getter
    @Column(length = 11, unique = true, nullable = false)
    private String cpf;

    @OneToOne
    @Getter
    @Setter
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Getter
    @Setter
    private String contato;

    @Getter
    @Setter
    private String email;

    @Getter
    @Setter
    private LocalDate dataNascimento;

    @Setter
    @Getter
    private Boolean ativo = true;


    public Cliente() {
    }

    public Cliente(String nome, String cpf, String contato, String email, LocalDate dataNascimento) {
        this.nome = nome;
        this.cpf = cpf;
        this.contato = contato;
        this.email = email;
        this.dataNascimento = dataNascimento;
    }


}
