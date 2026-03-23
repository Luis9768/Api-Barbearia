package com.barbearia.barbershop_api.dto.barbeiroDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DadosEntradaCadastroBarbeiro(
    @NotNull(message = "O nome do barbeiro é obrigatório!")
    String nome,
    @NotBlank(message = "O email é obrigatório!")
    @Email(message = "O e-mail deve ser válido!")
    String email,
    @NotNull(message = "O contato do barbeiro é obrigatório")
    String contato,
    @NotNull(message = "A senha é obrigatória!")
    String senha
) {
}
