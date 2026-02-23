package com.barbearia.barbershop_api.controller;

import com.barbearia.barbershop_api.dto.ClienteDTO;
import com.barbearia.barbershop_api.model.Cliente;
import com.barbearia.barbershop_api.model.Usuario;
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
    public ResponseEntity<ClienteDTO> cadastrarUsuario(@Valid @RequestBody ClienteDTO dto) {
        ClienteDTO usuario = service.cadastroUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }//cadastro de usuario, recebe requisições HTTP Json e manda para as regras de negocio service.

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listarUsuarios(@AuthenticationPrincipal Usuario usuarioLogado) {
        return ResponseEntity.ok(service.listarUsuarios(usuarioLogado));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/buscarPorNome")
    public ResponseEntity<List<ClienteDTO>> buscarClienteNome(@RequestParam String name, @AuthenticationPrincipal Usuario usuarioLogado){
        var buscarNome = service.pesquisarPorNome(name,  usuarioLogado);
        return ResponseEntity.ok(buscarNome.stream().map(ClienteDTO::new).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> atualizarDados(@PathVariable Integer id, @Valid @RequestBody ClienteDTO dto, @AuthenticationPrincipal Usuario usuarioLogado) {
        ClienteDTO usuarioAtualizado = service.atualizar(id, dto, usuarioLogado);
        if (usuarioAtualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Integer id, @AuthenticationPrincipal Usuario usuarioLogado) {
        service.excluirUsuarioId(id, usuarioLogado);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/buscarPorEmail")
    public ResponseEntity<Optional<ClienteDTO>> buscarPorEmail(@RequestParam String email,@AuthenticationPrincipal Usuario usuarioLogado){
        var buscarEmail = service.pesquisarPorEmail(email,usuarioLogado);
        return ResponseEntity.ok(buscarEmail);
    }


}
