package com.barbearia.barbershop_api.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.barbearia.barbershop_api.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}") // Pega a senha do application.properties
    private String secret;

    // MÉTOD0 1: Gerar o Token (Cria o Crachá)
    public String gerarToken(Usuario usuario) {
        try {
            var algoritmo = Algorithm.HMAC256(secret);
            String perfilString = (usuario.getPerfil() != null) ? usuario.getPerfil().toString() : "CLIENTE";
            return JWT.create()
                    .withIssuer("API Barbearia") // Quem emitiu?
                    .withSubject(usuario.getLogin()) // Quem é o dono? (Salva o e-mail dentro do token)
                    .withClaim("id",usuario.getId())
                    .withClaim("role",perfilString)
                    .withExpiresAt(dataExpiracao()) // Até quando vale?
                    .sign(algoritmo); // Assina digitalmente
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token jwt", exception);
        }
    }

    // MÉTOD0 2: Validar o Token (Lê o Crachá)
    public String getSubject(String tokenJWT) {
        try {
            var algoritmo = Algorithm.HMAC256(secret);
            return JWT.require(algoritmo)
                    .withIssuer("API Barbearia")
                    .build()
                    .verify(tokenJWT) // Verifica se o token é válido
                    .getSubject(); // Devolve o e-mail que estava escondido lá dentro
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Token JWT inválido ou expirado!");
        }
    }

    // Método auxiliar para definir validade de 5 horas
    private Instant dataExpiracao() {
        return LocalDateTime.now().plusHours(5).toInstant(ZoneOffset.of("-03:00"));
    }
}
