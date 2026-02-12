package com.barbearia.barbershop_api.controller;

import com.barbearia.barbershop_api.dto.SaidaAgendamentoDTO;
import com.barbearia.barbershop_api.dto.DadosEntradaCadastroAgendamento;
import com.barbearia.barbershop_api.dto.FaturamentoDTO;
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

    @PostMapping("/AgendarHorario")
    @Transactional
    public ResponseEntity<SaidaAgendamentoDTO> agendarHorario(@Valid @RequestBody DadosEntradaCadastroAgendamento dados,@AuthenticationPrincipal Usuario usuarioLogado){
        Agendamento salvarAgendamento = service.realizarAgendamento(dados, usuarioLogado);
        var dtoSaida = new SaidaAgendamentoDTO(salvarAgendamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(dtoSaida);
    }
    @GetMapping("/listarAgendamentos")
    public ResponseEntity<List<SaidaAgendamentoDTO>> listarAgendamentos(@RequestParam(required = false) LocalDate data, @AuthenticationPrincipal Cliente cliente){
        return ResponseEntity.ok(service.listarAgendamentos(data, cliente));
    }
    @GetMapping("/listarHorariosDisponiveis")
    public ResponseEntity<List<LocalTime>> listarHorariosDisponiveis(@RequestParam LocalDate data, @RequestParam Integer servicoId ){
        var listaHorarios = service.listarHorariosDisponiveis(data,servicoId);
        return ResponseEntity.ok(listaHorarios);
    }
    @GetMapping("/agendamentos/listarHistoricoCliente/{id}")
    public ResponseEntity<List<SaidaAgendamentoDTO>> listarHistoricoCliente(@PathVariable("id") Integer idCliente, @AuthenticationPrincipal Usuario usuarioLogado){
        if(usuarioLogado.getPerfil() == Perfil.CLIENTE){
            var cliente =repositoryCliente.findByUsuarioId(usuarioLogado.getId()).get();
            var lista = service.listarHistoricoCLiente(cliente.getId(), usuarioLogado);
            return ResponseEntity.ok(lista);
        } return ResponseEntity.badRequest().build();
    }
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> cancelarAgendamento(@PathVariable Integer id, @AuthenticationPrincipal Usuario usuarioLogado){
        service.cancelarAgendamento(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/faturamento")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FaturamentoDTO> faturamento(@RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate data, @AuthenticationPrincipal Usuario usuarioLogado){
        BigDecimal resultado = BigDecimal.valueOf(service.calcularFaturamento(data, usuarioLogado));
        FaturamentoDTO faturamentoDTO = new FaturamentoDTO();
        faturamentoDTO.setMensagem("Faturamento do dia "+data);
        faturamentoDTO.setValor(resultado);
        return ResponseEntity.ok(faturamentoDTO);
    }
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<SaidaAgendamentoDTO> reagendamento(@PathVariable Integer id, @RequestBody @Valid DadosEntradaCadastroAgendamento dados, @AuthenticationPrincipal Usuario usuarioLogado){
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
