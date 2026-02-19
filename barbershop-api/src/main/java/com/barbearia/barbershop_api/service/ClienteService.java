package com.barbearia.barbershop_api.service;

import com.barbearia.barbershop_api.dto.ClienteDTO;
import com.barbearia.barbershop_api.model.Agendamento;
import com.barbearia.barbershop_api.model.Cliente;
import com.barbearia.barbershop_api.model.Perfil;
import com.barbearia.barbershop_api.model.Usuario;
import com.barbearia.barbershop_api.repository.ClienteRepository;
import com.barbearia.barbershop_api.repository.UsuarioLoginRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;
    @Autowired
    private UsuarioLoginRepository usuarioLoginRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public ClienteDTO cadastroUsuario(ClienteDTO dto) {
        if (repository.findByCpf(dto.getCpf()).isPresent()) {
            throw new IllegalArgumentException("CPF já cadastrado!");
        }
        if (repository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado!");
        }
        Usuario usuario = new Usuario();
        usuario.setLogin(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        Usuario usuarioSalvo = usuarioLoginRepository.save(usuario);

        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setCpf(dto.getCpf());
        cliente.setContato(dto.getContato());
        cliente.setDataNascimento(dto.getDataNascimento());
        cliente.setEmail(dto.getEmail());
        cliente.setAtivo(true);

        cliente.setUsuario(usuarioSalvo);

        usuario.setPerfil(Perfil.CLIENTE);
        repository.save(cliente);

        return new ClienteDTO(cliente);
    }

    public List<ClienteDTO> listarUsuarios(Usuario usuarioLogado) {
        boolean ehAdmin = usuarioLogado.getPerfil() == Perfil.ADMIN;
        if(!ehAdmin){
            throw new IllegalArgumentException("Você não tem permissão para ver os usuários!");
        }
        return repository.findAll().stream()
                .map(ClienteDTO::new)
                .toList();
    }

    public ClienteDTO atualizar(Integer id, ClienteDTO dto, Usuario usuarioLogado) {
        Cliente clienteAntigo = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Cliente para atualizar não encontrado!"));
        boolean ehDono = clienteAntigo.getUsuario().getId().equals(usuarioLogado.getId());
        if(!ehDono){
            throw new IllegalArgumentException("Você não pode alterar os dados de outra pessoa!");
        }
        clienteAntigo.setNome(dto.getNome());
        clienteAntigo.setContato(dto.getContato());
        clienteAntigo.setCpf(dto.getCpf());
        clienteAntigo.setEmail(dto.getEmail());
        clienteAntigo.setDataNascimento(dto.getDataNascimento());
        clienteAntigo = repository.save(clienteAntigo);
        return new ClienteDTO(clienteAntigo);
    }

    public void excluirUsuarioId(Integer id, Usuario usuarioLogado) {
        Cliente cliente = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado!"));

        boolean ehAdmin = usuarioLogado.getPerfil() == Perfil.ADMIN;
        boolean ehDono = cliente.getUsuario().getId().equals(usuarioLogado.getId());
        if(!ehAdmin && !ehDono){
            throw new IllegalArgumentException("Você não tem permissão para deletar este usuário!");
        }

        cliente.setAtivo(false);

        repository.save(cliente);
    }



}
