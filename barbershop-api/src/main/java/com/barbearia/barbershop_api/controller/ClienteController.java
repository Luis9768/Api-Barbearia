package com.barbearia.barbershop_api.controller;

import com.barbearia.barbershop_api.dto.clienteDto.ClienteDTO;
import com.barbearia.barbershop_api.dto.clienteDto.DadosEntradaCadastroCliente;
import com.barbearia.barbershop_api.dto.clienteDto.DadosSaidaListaCLientes;
import com.barbearia.barbershop_api.dto.clienteDto.EntradaAtualizarCliente;
import com.barbearia.barbershop_api.entity.Usuario;
import com.barbearia.barbershop_api.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private ClienteService service; //injeta a classe clienteService aqui

    @PostMapping
    public ResponseEntity<DadosEntradaCadastroCliente> cadastrarUsuario (@Valid @RequestBody ClienteDTO dto) {
        DadosEntradaCadastroCliente usuario = service.cadastroUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @GetMapping
    public ResponseEntity<List<DadosSaidaListaCLientes>> listarUsuarios (@AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.ok(service.listarUsuarios(usuarioLogado));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/buscarPorNome/{nome}")
    public ResponseEntity<List<DadosSaidaListaCLientes>> buscarClienteNome (@PathVariable String nome, @AuthenticationPrincipal Usuario usuarioLogado){
        var buscarNome = service.pesquisarPorNome(nome,  usuarioLogado);
        return ResponseEntity.ok(buscarNome);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntradaAtualizarCliente> atualizarDados(@PathVariable Integer id, @Valid @RequestBody EntradaAtualizarCliente dto, @AuthenticationPrincipal Usuario usuarioLogado) {
        EntradaAtualizarCliente usuarioAtualizado = service.atualizar(id, dto, usuarioLogado);
        if (usuarioAtualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario (@PathVariable Integer id, @AuthenticationPrincipal Usuario usuarioLogado) {
        service.excluirUsuarioId(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/buscarPorEmail/{email}")
    public ResponseEntity<Optional<DadosSaidaListaCLientes>> buscarPorEmail(@PathVariable String email,@AuthenticationPrincipal Usuario usuarioLogado){
        var buscarEmail = service.pesquisarPorEmail(email,usuarioLogado);
        return ResponseEntity.ok(buscarEmail);
    }


}
