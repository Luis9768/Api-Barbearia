package com.barbearia.barbershop_api.infra;

import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity tratarErro400(MethodArgumentNotValidException ex){
        var erros = ex.getFieldErrors();
       return ResponseEntity.badRequest().body(
               erros.stream()
                .map(DadosErroValidacao::new)
                .toList()
       );
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity tratarErroData(MethodArgumentTypeMismatchException ex){
        String mensagem = ("O parametro "+ex.getName()+" recebeu um valor inválido "+ex.getValue()+".");
        return ResponseEntity.badRequest().body(mensagem);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity tratarArgumetosIlegais(IllegalArgumentException ex){
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // Um DTO simples para padronizar nosso erro
    private record DadosErroValidacao(String campo, String mensagem) {
        public DadosErroValidacao(FieldError erro) {
            this(erro.getField(), erro.getDefaultMessage());
        }
    }
}
