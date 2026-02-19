package com.barbearia.barbershop_api.controller;

import com.barbearia.barbershop_api.dto.ServicoDTO;
import com.barbearia.barbershop_api.model.Servico;
import com.barbearia.barbershop_api.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/servico")

public class ServicoController {
    @Autowired
    private ServicoService service; // injeta a classe Service aqui no controller

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Servico> cadastrar(@Valid @RequestBody ServicoDTO dto) {
        Servico servico = new Servico(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastro(servico));
        //serve pra receber requisições HTTP Json e mandar para as regras de negocio (Service)
    }

    @GetMapping
    public ResponseEntity<List<ServicoDTO>> listar() {

        return ResponseEntity.ok(service.listarTudo().stream()
                .map(ServicoDTO::new)
                .toList());
        //serve para mandar a lista do banco de dados seja ela vazia ou cheia
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoDTO> pesquisaPorId(@PathVariable Integer id) {
        Servico buscarId = service.buscarPorId(id);
        if (buscarId != null){
            ServicoDTO servicoDTO = new ServicoDTO(buscarId);
            return ResponseEntity.ok(servicoDTO);
        }else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity <List<ServicoDTO>> pesquisarPorNome(@RequestParam String nome){
        var buscarPorNome = service.buscarPorNome(nome);
        return ResponseEntity.ok(buscarPorNome.stream().map(ServicoDTO::new).toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Servico> atualizarServico(@PathVariable Integer id, @Valid @RequestBody ServicoDTO dto) {
        Servico servico = new Servico(dto);
        Servico buscarId = service.buscarPorId(id);
        if (buscarId == null) {
            return ResponseEntity.notFound().build();
        }
        Servico servicoAtualizado = service.atualizar(id, servico);
        return ResponseEntity.ok(servicoAtualizado);
        //metodo atualizar, primeiro cria uma variavel para pesquisar o id no banco,
        // e verifica se ele existe, e depois atualiza e retorna os dados atualizados
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarServico(@PathVariable Integer id) {
        Servico buscarId = service.buscarPorId(id);
        if (buscarId == null) {
            return ResponseEntity.notFound().build();
        }
        service.excluirId(id);
        return ResponseEntity.noContent().build();
        //nesse metodo como la no service ta void pq não retorna nada ele apenas busca o ID no banco
        //e se achar exclui e o ResponseEntity.noContent não retorna nada
    }

}
