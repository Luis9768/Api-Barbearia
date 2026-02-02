package com.barbearia.barbershop_api.repository;

import com.barbearia.barbershop_api.model.DiaEspecial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DiaEspecialRepository extends JpaRepository<DiaEspecial, Integer> {
    Optional<DiaEspecial> findByData(LocalDate data);
}
