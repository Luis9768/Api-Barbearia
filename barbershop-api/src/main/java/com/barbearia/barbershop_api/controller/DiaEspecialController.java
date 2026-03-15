package com.barbearia.barbershop_api.controller;

import com.barbearia.barbershop_api.dto.DadosEntradaDiaEspecial;
import com.barbearia.barbershop_api.dto.DadosEntradaReagendamento;
import com.barbearia.barbershop_api.dto.SaidaDiaEspecialDTO;
import com.barbearia.barbershop_api.model.DiaEspecial;
import com.barbearia.barbershop_api.model.Usuario;
import com.barbearia.barbershop_api.service.DiaEspecialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/diaEspecial")
@RequiredArgsConstructor
public class DiaEspecialController {

    private final DiaEspecialService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SaidaDiaEspecialDTO> criarDataEspecial(@RequestBody DadosEntradaDiaEspecial dto, @AuthenticationPrincipal Usuario usuarioLogado){
         SaidaDiaEspecialDTO diaEspecial = service.cadastro(dto,usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(diaEspecial);
    }//ele cria um dia especial na agenda no qual não abrirá

    @GetMapping
    public ResponseEntity<List<SaidaDiaEspecialDTO>> listarDiaEspecial(){
        var diaEspecial = service.listarDiaEspecial();
        return ResponseEntity.ok(diaEspecial.stream().map(SaidaDiaEspecialDTO::new).toList());
    }
    @GetMapping("/{id}")
    public ResponseEntity<DiaEspecial> buscarPorId(@PathVariable Integer id){
        var buscarDia = service.buscarPorId(id);
        return ResponseEntity.ok(buscarDia);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SaidaDiaEspecialDTO> atualizarDiaEspecial(@PathVariable Integer id, @RequestBody DadosEntradaDiaEspecial dados, @AuthenticationPrincipal Usuario usuarioLogado){
        SaidaDiaEspecialDTO diaEspecial = service.atualizarDiaEspecial(id,dados,usuarioLogado);
        return ResponseEntity.ok(diaEspecial);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluirDiaEspecial(@PathVariable int id,@AuthenticationPrincipal Usuario usuarioLogado){
        service.deletarDiaEspecial(id,usuarioLogado);
        return ResponseEntity.noContent().build();
    }







}
