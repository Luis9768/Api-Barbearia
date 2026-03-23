package com.barbearia.barbershop_api.repository;

import com.barbearia.barbershop_api.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByCpf(String cpf);
    Optional<Cliente> findByEmail(String email);
    Optional<Cliente> findByUsuarioId(Integer id);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
    boolean existsByContato(String contato);
    List<Cliente> findByNomeContainingIgnoreCase(String name);

}