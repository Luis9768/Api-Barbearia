package com.barbearia.barbershop_api.repository;

import com.barbearia.barbershop_api.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico,Integer> {
    //essa interface vai fazer os incerts no banco.
}