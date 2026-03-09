package com.barbearia.barbershop_api.repository;

import com.barbearia.barbershop_api.model.Barbeiro;
import com.barbearia.barbershop_api.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BarbeiroRepository extends JpaRepository<Barbeiro, Integer>{
    Optional<Cliente> findByEmail(String email);
    Optional<Cliente> findByUsuarioId(Integer id);
    boolean existsByEmail(String email);

}