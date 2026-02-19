package com.barbearia.barbershop_api.infra.security;

import com.barbearia.barbershop_api.repository.UsuarioLoginRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioLoginRepository repository;



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Pega o token do cabeçalho (Authorization: Bearer xxxxx)
        var tokenJWT = recuperarToken(request);
        System.out.println("Token recebido: " + tokenJWT); // Log para depuração

        if (tokenJWT != null) {
            // 2. Valida o token e pega o e-mail (subject)
            var login = tokenService.getSubject(tokenJWT);
            System.out.println("🚨 [FILTRO] Token válido! Email/Login encontrado: " + login);

            // 3. Busca o usuário no banco
            var usuario = repository.findByLogin(login); // Se mudou pra findByEmail, ajuste aqui!

            if (usuario != null) {
                System.out.println("🚨 [FILTRO] Usuário carregado do banco com sucesso! Perfil: " + usuario.getAuthorities()); // E isso
                var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }// Cria a autenticação do Spring e força o login
        }
        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        var authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null) {
            return authorizationHeader.replace("Bearer ", "").trim();
        }
        return null;
    }
}
