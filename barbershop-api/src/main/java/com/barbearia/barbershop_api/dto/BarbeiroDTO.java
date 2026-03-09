package com.barbearia.barbershop_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class BarbeiroDTO {
    @NotBlank(message = "O nome é obrigatório!")
    private String nome;

    @NotBlank(message = "O contato é obrigatório!")
    private String contato;

    @NotBlank(message = "O email é obrigatório!")
    @Email(message = "O e-mail deve ser válido!")
    private String email;

    private String senha;
}
