package com.barbearia.barbershop_api.service;

import com.barbearia.barbershop_api.dto.BarbeiroDTO;
import com.barbearia.barbershop_api.dto.DadosEntradaCadastroBarbeiro;
import com.barbearia.barbershop_api.model.Barbeiro;
import com.barbearia.barbershop_api.model.Perfil;
import com.barbearia.barbershop_api.model.Usuario;
import com.barbearia.barbershop_api.repository.BarbeiroRepository;
import com.barbearia.barbershop_api.repository.UsuarioLoginRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BarbeiroService {
    @Autowired
    private BarbeiroRepository repository;
    @Autowired
    private UsuarioLoginRepository usuarioLoginRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public BarbeiroDTO adicionar(DadosEntradaCadastroBarbeiro dto){
        if(repository.existsByEmail(dto.email())){
            throw new IllegalArgumentException("Email já cadastrado!");
        }
        Usuario usuario = new Usuario();
        usuario.setLogin(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        Usuario usuarioSalvo = usuarioLoginRepository.save(usuario);

        Barbeiro barbeiro = new Barbeiro();
        barbeiro.setNome(dto.nome());
        barbeiro.setContato(dto.contato());
        barbeiro.setEmail(dto.email());
        barbeiro.setSenha(dto.senha());
        barbeiro.setAtivo(true);

        barbeiro.setUsuario(usuarioSalvo);

        usuario.setPerfil(Perfil.ADMIN);
        repository.save(barbeiro);

        return new DadosEntradaCadastroBarbeiro(barbeiro);


    }

}
