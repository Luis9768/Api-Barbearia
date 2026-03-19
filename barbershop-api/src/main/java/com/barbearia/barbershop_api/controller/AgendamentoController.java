package com.barbearia.barbershop_api.controller;

import com.barbearia.barbershop_api.dto.*;
import com.barbearia.barbershop_api.model.Agendamento;
import com.barbearia.barbershop_api.model.Cliente;
import com.barbearia.barbershop_api.model.Perfil;
import com.barbearia.barbershop_api.model.Usuario;
import com.barbearia.barbershop_api.repository.ClienteRepository;
import com.barbearia.barbershop_api.repository.ItemRankingDTO;
import com.barbearia.barbershop_api.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.PublicKey;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/agendamento")

public class AgendamentoController {
    @Autowired
    private AgendamentoService service;
    @Autowired
    private ClienteRepository repositoryCliente;

    @PostMapping("/Adicionar")
    @Transactional
    public ResponseEntity<DadosSaidaAgendamento> agendarHorario(@Valid @RequestBody DadosEntradaCadastroAgendamento dados, @AuthenticationPrincipal Usuario usuarioLogado){
        DadosSaidaAgendamento salvarAgendamento = service.realizarAgendamento(dados, usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvarAgendamento);
    }
    @GetMapping("/ListarAgendamentosPorCliente")
    public ResponseEntity<List<DadosSaidaAgendamento>> listarAgendamentosPorCLiente(@RequestParam Integer id, @AuthenticationPrincipal Usuario usuario){
        List<DadosSaidaAgendamento> lista = service.listarAgendamentoPorCliente(id,usuario);
        return ResponseEntity.ok(lista);
    }
    @GetMapping("/ListarAgendamentosPorData")
    public ResponseEntity<List<DadosSaidaAgendamento>> listarAgendamentosPorData(@RequestParam LocalDate data, @AuthenticationPrincipal Usuario usuario){
        List<DadosSaidaAgendamento> lista = service.listarAgendamentosPorData(data, usuario);
        return  ResponseEntity.ok(lista);
    }

    @GetMapping("/listarHorariosDisponiveis")
    public ResponseEntity<List<LocalTime>> listarHorariosDisponiveis(@RequestParam LocalDate data, @RequestParam Integer servicoId ){
        var listaHorarios = service.listarHorariosDisponiveis(data,servicoId);
        return ResponseEntity.ok(listaHorarios);
    }
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> cancelarAgendamento(@PathVariable Integer id, @AuthenticationPrincipal Usuario usuarioLogado){
        service.cancelarAgendamento(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<SaidaAgendamentoDTO> reagendamento(@PathVariable Integer id, @RequestBody @Valid DadosEntradaReagendamento dados, @AuthenticationPrincipal Usuario usuarioLogado){
        Agendamento agendamentoAtualizado = service.reagendar(id, dados, usuarioLogado);
        return ResponseEntity.ok(new SaidaAgendamentoDTO(agendamentoAtualizado));
    }
    @GetMapping("/rankingAgendamentos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ItemRankingDTO>> buscarRanking(){
        var buscarLista = service.listarRankingServicos();
        return ResponseEntity.ok(buscarLista);
    }
}
