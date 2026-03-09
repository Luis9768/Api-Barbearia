package com.barbearia.barbershop_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "barbeiro")
@SQLRestriction("ativo = true")
@SQLDelete(sql = "UPDATE barbeiro SET ativo = false WHERE id = ?")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Barbeiro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nome;

    @JoinColumn(name = "usuario_id")
    @OneToOne
    private Usuario usuario;

    private String email;

    private String contato;

    private Boolean ativo = true;

    private String senha;
}
