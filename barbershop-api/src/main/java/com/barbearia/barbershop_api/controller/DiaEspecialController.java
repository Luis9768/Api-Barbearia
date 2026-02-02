package com.barbearia.barbershop_api.controller;

import com.barbearia.barbershop_api.dto.DadosEntradaDiaEspecial;
import com.barbearia.barbershop_api.model.DiaEspecial;
import com.barbearia.barbershop_api.service.DiaEspecialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/diaEspecial")
@RequiredArgsConstructor
public class DiaEspecialController {

    private final DiaEspecialService service;

    @PostMapping
    public ResponseEntity<DiaEspecial> criarDataEspecial(@RequestBody DadosEntradaDiaEspecial dto){
        DiaEspecial diaEspecial = service.cadastro(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(diaEspecial);
    }//ele cria um dia especial na agenda no qual não abrirá



}
