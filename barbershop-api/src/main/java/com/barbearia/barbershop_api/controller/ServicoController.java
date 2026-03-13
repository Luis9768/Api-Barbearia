package com.barbearia.barbershop_api.controller;

import com.barbearia.barbershop_api.dto.ServicoDTO;
import com.barbearia.barbershop_api.model.Servico;
import com.barbearia.barbershop_api.service.ServicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/servico")

public class ServicoController {
    @Autowired
    private ServicoService service; // injeta a classe Service aqui no controller

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicoDTO> cadastrar(@RequestPart("dados") ServicoDTO dto, @RequestPart("imagem")MultipartFile arquivo) {
        Servico servico = new Servico(dto);
        service.cadastro(servico,arquivo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
        //serve pra receber requisições HTTP Json e mandar para as regras de negocio (Service)
    }

    @GetMapping("/listar")
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
    @GetMapping("/{id}/imagem")
    public ResponseEntity<byte[]> pesquisarImagem(@PathVariable Integer id){
        var buscar = service.buscarPorId(id);
        if(buscar.getDadosImagem() ==  null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(buscar.getTipoImagem()))
                .body(buscar.getDadosImagem());
    }

    @GetMapping("/buscarPorNome")
    public ResponseEntity <List<ServicoDTO>> pesquisarPorNome(@RequestParam String nome){
        var buscarPorNome = service.buscarPorNome(nome);
        return ResponseEntity.ok(buscarPorNome.stream().map(ServicoDTO::new).toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServicoDTO> atualizarServico(@PathVariable Integer id, @Valid @RequestPart("dados") ServicoDTO dto,@RequestPart(value = "imagem",required = false)MultipartFile arquivo) {
        ServicoDTO servicoAtualizado = service.atualizar(id, dto,arquivo);
        return ResponseEntity.ok(servicoAtualizado);
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
    }

}
