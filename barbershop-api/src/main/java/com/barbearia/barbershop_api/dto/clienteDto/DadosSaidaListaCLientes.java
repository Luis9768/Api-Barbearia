package com.barbearia.barbershop_api.dto.clienteDto;

import com.barbearia.barbershop_api.entity.Cliente;

public record DadosSaidaListaCLientes(
        String nome,
        String contato,
        String cpf,
        String email
) {
    public DadosSaidaListaCLientes(Cliente cliente){
        this(
                cliente.getNome(),
                cliente.getContato(),
                cliente.getCpf(),
                cliente.getEmail()
        );
    }
}
