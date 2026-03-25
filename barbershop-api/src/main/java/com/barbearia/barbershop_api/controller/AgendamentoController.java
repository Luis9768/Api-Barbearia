package com.barbearia.barbershop_api.controller;

import com.barbearia.barbershop_api.dto.agendamentoDto.DadosEntradaCadastroAgendamento;
import com.barbearia.barbershop_api.dto.agendamentoDto.DadosEntradaReagendamento;
import com.barbearia.barbershop_api.dto.agendamentoDto.DadosSaidaAgendamento;
import com.barbearia.barbershop_api.entity.Usuario;
import com.barbearia.barbershop_api.repository.ClienteRepository;
import com.barbearia.barbershop_api.repository.ItemRankingDTO;
import com.barbearia.barbershop_api.service.AgendamentoService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public ResponseEntity<List<DadosSaidaAgendamento>> listarAgendamentosPorDataBarbeiro(@RequestParam LocalDate data, @RequestParam Integer barbeiroId, @AuthenticationPrincipal Usuario usuario){
        List<DadosSaidaAgendamento> lista = service.listarAgendamentosPorDataBarbeiro(data, barbeiroId, usuario);
        return  ResponseEntity.ok(lista);
    }

    @GetMapping("/listarHorariosDisponiveis")
    public ResponseEntity<List<LocalTime>> listarHorariosDisponiveis(@RequestParam LocalDate data, @RequestParam Integer servicoId, @RequestParam Integer barbeiroId ){
        var listaHorarios = service.listarHorariosDisponiveis(barbeiroId,data,servicoId);
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
    public ResponseEntity<DadosEntradaReagendamento> reagendamento(@PathVariable Integer id, @RequestBody @Valid DadosEntradaReagendamento dados, @AuthenticationPrincipal Usuario usuarioLogado){
        DadosEntradaReagendamento agendamentoAtualizado = service.reagendar(id, dados, usuarioLogado);
        return ResponseEntity.ok(agendamentoAtualizado);
    }
    @GetMapping("/rankingAgendamentos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ItemRankingDTO>> buscarRanking(){
        var buscarLista = service.listarRankingServicos();
        return ResponseEntity.ok(buscarLista);
    }
}
