package com.barbearia.barbershop_api.service;

import com.barbearia.barbershop_api.dto.clienteDto.ClienteDTO;
import com.barbearia.barbershop_api.dto.clienteDto.DadosEntradaCadastroCliente;
import com.barbearia.barbershop_api.dto.clienteDto.DadosSaidaListaCLientes;
import com.barbearia.barbershop_api.dto.clienteDto.EntradaAtualizarCliente;
import com.barbearia.barbershop_api.entity.Cliente;
import com.barbearia.barbershop_api.entity.Perfil;
import com.barbearia.barbershop_api.entity.Usuario;
import com.barbearia.barbershop_api.repository.ClienteRepository;
import com.barbearia.barbershop_api.repository.UsuarioLoginRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository repository;
    @Autowired
    private UsuarioLoginRepository usuarioLoginRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public DadosEntradaCadastroCliente cadastroUsuario(ClienteDTO dto) {
        if (repository.existsByCpf(dto.getCpf())) {
            throw new IllegalArgumentException("Já existe um cliente cadastrado com este CPF/Dado!");
        }
        if (repository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Já existe um cliente cadastrado com este Email/Dado!");
        }
        if (repository.existsByContato(dto.getContato())) {
            throw new IllegalArgumentException("Já existe um cliente cadastrado com este Contato!");
        }

        Usuario usuario = new Usuario();
        usuario.setLogin(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setAtivo(true);
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

        return new DadosEntradaCadastroCliente(cliente);
    }

    public List<DadosSaidaListaCLientes> listarUsuarios(Usuario usuarioLogado) {
        boolean ehAdmin = usuarioLogado.getPerfil() == Perfil.ADMIN;
        if (!ehAdmin) {
            throw new IllegalArgumentException("Você não tem permissão para ver os usuários!");
        }
        return repository.findAll().stream()
                .map(DadosSaidaListaCLientes::new)
                .toList();
    }

    public EntradaAtualizarCliente atualizar(Integer id, EntradaAtualizarCliente dto, Usuario usuarioLogado) {
        Cliente clienteAntigo = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Cliente para atualizar não encontrado!"));
        boolean ehDono = clienteAntigo.getUsuario().getId().equals(usuarioLogado.getId());
        if (!ehDono) {
            throw new IllegalArgumentException("Você não pode alterar os dados de outra pessoa!");
        }
        if (dto.cpf() != null && !dto.cpf().isBlank()) {
            Optional<Cliente> existeCpf = repository.findByCpf(dto.cpf());
            if (existeCpf.isPresent() && !existeCpf.get().getId().equals(clienteAntigo.getId())) {
                throw new IllegalArgumentException("Erro: Este CPF já pertence a outro cliente no sistema!");
            }
        }
        if(dto.nome() != null && !dto.nome().isBlank()) {
            clienteAntigo.setNome(dto.nome());
        }
        if(dto.contato() !=null && !dto.contato().isBlank()) {
            clienteAntigo.setContato(dto.contato());
        }
        if (dto.cpf() != null && !dto.cpf().isBlank()) {
            clienteAntigo.setCpf(dto.cpf());
        }
        if(dto.email() != null && !dto.email().isBlank()) {
            clienteAntigo.setEmail(dto.email());
            clienteAntigo.getUsuario().setLogin(dto.email());
        }
        if(dto.dataNascimento() != null) {
            clienteAntigo.setDataNascimento(dto.dataNascimento());
        }
        if(dto.senha() != null && !dto.senha().isBlank()){
           String senha = passwordEncoder.encode(dto.senha());
           clienteAntigo.getUsuario().setSenha(senha);
        }
        clienteAntigo = repository.save(clienteAntigo);

        return new EntradaAtualizarCliente(clienteAntigo);
    }

    public void excluirUsuarioId(Integer idAlvo, Usuario usuarioLogado) {

        Usuario usuarioAlvo = usuarioLoginRepository.findById(idAlvo).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado!"));

        boolean ehAdmin = usuarioLogado.getPerfil() == Perfil.ADMIN;
        boolean ehDono = usuarioAlvo.getId().equals(usuarioLogado.getId());
        if (!ehAdmin && !ehDono) {
            throw new IllegalArgumentException("Você não tem permissão para deletar este usuário!");
        }

        Cliente clienteAlvo = repository.findByUsuarioId(usuarioAlvo.getId()).orElseThrow(() -> new IllegalArgumentException("Cliente atrelado a este usuário não encontrado!"));

        usuarioAlvo.setAtivo(false);
        clienteAlvo.setAtivo(false);

        usuarioLoginRepository.save(usuarioAlvo);
        repository.save(clienteAlvo);
    }

    public List<DadosSaidaListaCLientes> pesquisarPorNome(String nome, Usuario usuarioLogado) {
        if (usuarioLogado.getPerfil() == Perfil.ADMIN) {
            return repository.findByNomeContainingIgnoreCase(nome).stream().map(DadosSaidaListaCLientes::new).toList();
        } else {
            throw new IllegalArgumentException("Você não tem permissão para pesquisar usuários!");
        }
    }
    public Optional<DadosSaidaListaCLientes> pesquisarPorEmail(String email, Usuario usuarioLogado){
        if(usuarioLogado.getPerfil() == Perfil.ADMIN){
            return repository.findByEmail(email).map(DadosSaidaListaCLientes::new);
        }else {
            throw new IllegalArgumentException("Você não tem permissão para pesquisar usuários!");
        }
    }
}




