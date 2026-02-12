package com.barbearia.barbershop_api.repository;

import com.barbearia.barbershop_api.model.Agendamento;
import com.barbearia.barbershop_api.model.Cliente;
import com.barbearia.barbershop_api.model.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {
    // Mude de boolean para Long
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE " +
            "(:inicio < a.dataHoraFim) AND (:fim > a.dataHoraInicio)")
    Integer contarConflitos(@Param("inicio") LocalDateTime inicio,
                            @Param("fim") LocalDateTime fim);

    @Query("""
    SELECT SUM(s.preco) 
    FROM Agendamento a 
    JOIN a.servico s 
    WHERE a.dataHoraInicio BETWEEN :inicio AND :fim 
    AND a.statusAgendamento = :status
""") Double somarFaturamentoPorStatus(LocalDateTime inicio, LocalDateTime fim, StatusAgendamento status);

    List<Agendamento> findByDataHoraInicioBetween(LocalDateTime inicio, LocalDateTime fim);

    @Query("select count(a) from Agendamento a where a.dataHoraInicio < :dataFim and a.dataHoraFim > :dataInicio and a.id <> :id")
    Integer contarConflitosParaReagendamento(LocalDateTime dataInicio, LocalDateTime dataFim, Integer id);

    @Query("SELECT COUNT(a) > 0 FROM Agendamento a WHERE a.servico.id = :servicoId AND a.dataHoraInicio > CURRENT_TIMESTAMP AND a.ativo = true")
    boolean existeAgendamentoFuturoParaOServico(Integer servicoId);

    @Query("SELECT a FROM Agendamento a " +
            "WHERE a.dataHoraInicio < :fim AND a.dataHoraFim > :inicio")
    List<Agendamento> findConflitos(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    List<Agendamento> findByClienteIdOrderByDataHoraInicioDesc(Integer clienteId);

    @Query("SELECT s.nome AS nome, COUNT(a) AS quantidade FROM Agendamento a JOIN a.servico s GROUP BY s.nome ORDER BY quantidade DESC")
    List<ItemRankingDTO> findRankingServicos();

    List<Agendamento> findByClienteAndDataHoraInicioBetween(Cliente cliente, LocalDateTime inicio, LocalDateTime fim);
}
