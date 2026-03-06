package com.barbearia.barbershop_api.controller;

import com.barbearia.barbershop_api.dto.HIstoricoServicoDTO;
import com.barbearia.barbershop_api.model.Usuario;
import com.barbearia.barbershop_api.service.ServicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/historico")
public class LogAlteracaoController {
    @Autowired
    private ServicoService service;

    @GetMapping("/historico/{id}/historico")
    public ResponseEntity<List<HIstoricoServicoDTO>> listar(@RequestParam Long id, @AuthenticationPrincipal Usuario usuario){
        var lista = service.buscarHistorico(id, usuario);
        return ResponseEntity.ok(lista);
    }
}
