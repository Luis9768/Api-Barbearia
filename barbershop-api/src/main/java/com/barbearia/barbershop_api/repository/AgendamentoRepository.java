package com.barbearia.barbershop_api.repository;

import com.barbearia.barbershop_api.entity.Agendamento;
import com.barbearia.barbershop_api.entity.Cliente;
import com.barbearia.barbershop_api.entity.StatusAgendamento;
import org.antlr.v4.runtime.atn.SemanticContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {
    // Mude de boolean para Long
    @Query("SELECT COUNT(a) FROM Agendamento a WHERE " +
            "(:inicio < a.dataHoraFim) AND (:fim > a.dataHoraInicio)")
    Integer contarConflitos(@Param("inicio") LocalDateTime inicio,
                            @Param("fim") LocalDateTime fim);

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

    @Query(value = "SELECT * FROM agendamento WHERE cliente_id = :clienteId", nativeQuery = true)
    List<Agendamento> buscarHistoricoCompleto(Integer clienteId);

    // Resultado: Traz TUDO do banco (agendados, cancelados, concluídos), furando o bloqueio da sua classe.
    List<Agendamento> findByStatusAgendamentoAndDataHoraFimBefore(StatusAgendamento status, LocalDateTime agora);

    @Query("SELECT COUNT(a) > 0 FROM Agendamento a " +
            "WHERE a.barbeiro.id = :barbeiroId " +
            "AND a.dataHoraInicio < :dataFim " +
            "AND a.dataHoraFim > :dataInicio " +
            "AND a.statusAgendamento != 'CANCELADO'") // Bônus Sênior: Ignora horários cancelados!
    boolean existeConflitoHorario(
            @Param("barbeiroId") Integer barbeiroId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim
    );
    @Query("SELECT a FROM Agendamento a WHERE a.cliente.id = :clienteId ORDER BY a.dataHoraInicio DESC")
    List<Agendamento> findByClienteId(@Param("clienteId") Integer clienteId);

    @Query("SELECT a FROM Agendamento a WHERE a.cliente = :cliente AND a.dataHoraInicio BETWEEN :inicio AND :fim ORDER BY a.dataHoraInicio ASC")
    List<Agendamento> findByClienteAndDataHoraInicioBetween(
            @Param("cliente") Cliente cliente,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
    // Busca todos os agendamentos de um barbeiro específico (ignorando os cancelados!)
    @Query("SELECT a FROM Agendamento a WHERE a.barbeiro.id = :barbeiroId AND DATE(a.dataHoraInicio) = :data AND a.statusAgendamento != 'CANCELADO'")
    List<Agendamento> buscarPorBarbeiroEData(
            @Param("barbeiroId") Integer barbeiroId,
            @Param("data") LocalDate data
    );
    @Query("""
            SELECT a
            FROM Agendamento a
            WHERE a.barbeiro.id = :barbeiroId
              AND a.dataHoraInicio >= :inicio
              AND a.dataHoraInicio < :fim
            ORDER BY a.dataHoraInicio ASC
            """)
    List<Agendamento> listarTodosPorBarbeiroEData(
            @Param("barbeiroId") Integer barbeiroId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
    @Query(value = """
    SELECT *
    FROM agendamento
    WHERE data_hora_inicio >= :inicio
      AND data_hora_inicio < :fim
      AND ativo = true
      AND status_agendamento IN ('AGENDADO', 'CANCELADO', 'CONCLUIDO')
    ORDER BY data_hora_inicio ASC
""", nativeQuery = true)
    List<Agendamento> listarTodosPorDataNativo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    @Query(value = """
    SELECT *
    FROM agendamento
    WHERE cliente_id = :clienteId
      AND data_hora_inicio >= :inicio
      AND data_hora_inicio < :fim
      AND ativo = true
      AND status_agendamento IN ('AGENDADO', 'CANCELADO', 'CONCLUIDO')
    ORDER BY data_hora_inicio ASC
""", nativeQuery = true)
    List<Agendamento> listarTodosPorClienteEData(
            @Param("clienteId") Integer clienteId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );
}
