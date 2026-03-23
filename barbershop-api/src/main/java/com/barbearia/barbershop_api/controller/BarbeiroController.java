package com.barbearia.barbershop_api.controller;

import com.barbearia.barbershop_api.dto.barbeiroDto.BarbeiroDto;
import com.barbearia.barbershop_api.dto.barbeiroDto.DadosEntradaAtualizarBarbeiro;
import com.barbearia.barbershop_api.dto.barbeiroDto.DadosEntradaCadastroBarbeiro;
import com.barbearia.barbershop_api.entity.Usuario;
import com.barbearia.barbershop_api.service.BarbeiroService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/barbeiro")
public class BarbeiroController {
    @Autowired
    private BarbeiroService service;

    @PostMapping
    public ResponseEntity<BarbeiroDto> adicionar(@AuthenticationPrincipal Usuario usuarioLogado, @Valid @RequestBody DadosEntradaCadastroBarbeiro dto){
        BarbeiroDto dto1 = service.adicionar(dto,usuarioLogado);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto1);
    }
    @PutMapping("/{id}")
    public ResponseEntity<BarbeiroDto> atualizar(@PathVariable int id, @AuthenticationPrincipal Usuario usuarioLogado, @RequestBody DadosEntradaAtualizarBarbeiro dto){
        BarbeiroDto dto1 = service.atualizar(id,dto,usuarioLogado);
        return ResponseEntity.status(HttpStatus.OK).body(dto1);
    }
    @GetMapping("/listar")
    public ResponseEntity<List<BarbeiroDto>> listar(@AuthenticationPrincipal Usuario usuarioLogado){
        List<BarbeiroDto> lista = service.listarBarbeiros(usuarioLogado);
        return ResponseEntity.ok(lista);
    }
    @GetMapping("/{id}")
    public ResponseEntity<BarbeiroDto> listarPorID(@PathVariable int id, @AuthenticationPrincipal Usuario usuarioLogado){
        var listarId = service.buscarPorId(id, usuarioLogado);
        return ResponseEntity.ok(listarId);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable int id, @AuthenticationPrincipal Usuario usuarioLogado){
         service.deletar(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }
}
