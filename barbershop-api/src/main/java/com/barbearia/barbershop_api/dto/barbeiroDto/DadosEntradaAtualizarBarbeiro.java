package com.barbearia.barbershop_api.dto.barbeiroDto;

import jakarta.validation.constraints.Email;

public record DadosEntradaAtualizarBarbeiro(
        String nome,
        @Email(message = "O e-mail deve ser válido!")
        String email,
        String contato,
        String senha) {
}
