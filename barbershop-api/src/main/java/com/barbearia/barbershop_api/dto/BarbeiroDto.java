package com.barbearia.barbershop_api.dto;

import com.barbearia.barbershop_api.model.Barbeiro;

public record BarbeiroDto(
        Integer id,
        String nome,
        String contato,
        String email
) {
    public BarbeiroDto(Barbeiro barbeiro){
        this(barbeiro.getId(), barbeiro.getNome(),barbeiro.getContato(), barbeiro.getEmail());
    }
}
