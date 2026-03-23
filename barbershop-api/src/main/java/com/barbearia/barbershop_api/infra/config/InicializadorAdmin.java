package com.barbearia.barbershop_api.infra.config;

import com.barbearia.barbershop_api.entity.Perfil;
import com.barbearia.barbershop_api.entity.Usuario;
import com.barbearia.barbershop_api.repository.UsuarioLoginRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InicializadorAdmin {
    @Bean
    public CommandLineRunner inicializacaoAdmin(UsuarioLoginRepository repository, PasswordEncoder passwordEncoder){
        return args -> {
            if (repository.count() == 0) {
                Usuario admin = new Usuario();
                admin.setLogin("admin@email.com");
                admin.setSenha(passwordEncoder.encode("123456"));
                admin.setPerfil(Perfil.ADMIN);
                repository.save(admin);

                System.out.println("==================================================");
                System.out.println("🚀 USUÁRIO MESTRE CRIADO COM SUCESSO!");
                System.out.println("📧 Login: admin@barbearia.com");
                System.out.println("🔑 Senha: 123456");
                System.out.println("==================================================");
            }
        };
    }
}
