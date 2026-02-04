package com.barbearia.barbershop_api.controller;

import com.barbearia.barbershop_api.dto.ClienteDTO;
import com.barbearia.barbershop_api.model.Cliente;
import com.barbearia.barbershop_api.model.Usuario;
import com.barbearia.barbershop_api.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

}
