package com.barbearia.barbershop_api.dto.barbeiroDto;

import com.barbearia.barbershop_api.entity.Barbeiro;

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
