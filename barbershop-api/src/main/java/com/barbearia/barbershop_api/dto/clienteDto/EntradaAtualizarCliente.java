package com.barbearia.barbershop_api.dto.clienteDto;

import com.barbearia.barbershop_api.entity.Cliente;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;

public record EntradaAtualizarCliente(
        String nome,
        @Email(message = "O e-mail deve ser válido!")
        String email,
        String contato,
        String senha,
        String cpf,
        LocalDate dataNascimento
) {
    public EntradaAtualizarCliente(Cliente cliente) {
        this(cliente.getNome(), cliente.getEmail(), cliente.getContato(), null, cliente.getCpf(), cliente.getDataNascimento());
    }
}
