package com.barbearia.barbershop_api.infra;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MensagemErro> tratarErro(IllegalArgumentException ex) {
        MensagemErro erro = new MensagemErro();
        erro.setStatus(HttpStatus.CONFLICT.value()); // Aqui guardamos o número 409
        erro.setMensagem(ex.getMessage());           // Aqui guardamos a frase do erro que está na classe MensagemErro

        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<java.util.Map<String, String>> tratarErroValidacao(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        java.util.Map<String, String> erros = new java.util.HashMap<>();

        // Esse loop pega cada campo que falhou e a mensagem dele
        ex.getBindingResult().getFieldErrors().forEach(erro -> {
            erros.put(erro.getField(), erro.getDefaultMessage());
        });

        return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(erros);
    }
}
