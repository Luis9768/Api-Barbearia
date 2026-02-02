package com.barbearia.barbershop_api.dto;

import com.barbearia.barbershop_api.model.Cliente;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

import java.rmi.server.UID;
import java.time.LocalDate;

@Getter
@Setter
public class ClienteDTO {


    @NotBlank(message = "O nome é obrigatório!")
    private String nome;

    @NotBlank(message = "O CPF é obrigatório!")
    @CPF(message = "CPF inválido!")
    private String cpf;

    @NotBlank(message = "O contato é obrigatório!")
    private String contato;

    @NotBlank(message = "O email é obrigatório!")
    @Email(message = "O e-mail deve ser válido!")
    private String email;

    @NotNull(message = "A data de nascimento é obrigatória!")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataNascimento;

    private String senha;

    public ClienteDTO() {
    }

    // 2. Construtor para converter Entidade -> DTO (Usado no Service)
    public ClienteDTO(Cliente cliente) {
        this.nome = cliente.getNome();
        this.cpf = cliente.getCpf();
        this.contato = cliente.getContato();
        this.email = cliente.getEmail();
        this.dataNascimento = cliente.getDataNascimento();
    }
}

