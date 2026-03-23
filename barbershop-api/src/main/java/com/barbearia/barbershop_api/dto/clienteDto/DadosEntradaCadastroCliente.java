package com.barbearia.barbershop_api.dto.clienteDto;

import com.barbearia.barbershop_api.entity.Cliente;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DadosEntradaCadastroCliente(
        @NotBlank(message = "O nome é obrigatório!") // Adicionei pela lógica do seu construtor
        String nome,

        String cpf,

        @NotBlank(message = "O contato é obrigatório!")
        String contato,

        @NotBlank(message = "O email é obrigatório!")
        @Email(message = "O e-mail deve ser válido!")
        String email,

        @NotNull(message = "A data de nascimento é obrigatória!")
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate dataNascimento,

        String senha
) {
    public DadosEntradaCadastroCliente(Cliente cliente) {
        this(
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getContato(),
                cliente.getEmail(),
                cliente.getDataNascimento(),
                null // 🚨 Dica Sênior: Nunca devolva a senha do banco para a tela! Deixe nulo no DTO de saída.
        );
    }
}
