package com.barbearia.barbershop_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DadosEntradaAtualizarBarbeiro(
        String nome,
        @Email(message = "O e-mail deve ser válido!")
        String email,
        String contato,
        String senha) {
}
