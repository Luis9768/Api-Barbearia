package com.barbearia.barbershop_api.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Libera para TODAS as rotas da sua API
                .allowedOrigins("http://localhost:5173") // O endereço do seu Front-end
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Libera os verbos
                .allowedHeaders("*") // Libera todos os cabeçalhos
                .allowCredentials(true); // Necessário se for usar cookies ou tokens de autenticação
    }
}
