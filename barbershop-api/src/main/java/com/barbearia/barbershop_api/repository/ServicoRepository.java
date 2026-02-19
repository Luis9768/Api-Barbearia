package com.barbearia.barbershop_api.repository;

import com.barbearia.barbershop_api.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico,Integer> {
    //essa interface vai fazer os incerts no banco.
    List<Servico> findByNomeContainingIgnoreCase(String nome);
}