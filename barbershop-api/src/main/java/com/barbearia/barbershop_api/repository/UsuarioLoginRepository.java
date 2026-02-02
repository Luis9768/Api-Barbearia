package com.barbearia.barbershop_api.repository;

import com.barbearia.barbershop_api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UsuarioLoginRepository extends JpaRepository<Usuario, Integer> {
    UserDetails findByLogin(String login);
}
