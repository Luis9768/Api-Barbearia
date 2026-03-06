package com.barbearia.barbershop_api.service;

import com.barbearia.barbershop_api.dto.HIstoricoServicoDTO;
import com.barbearia.barbershop_api.model.LogAlteracaoServico;
import com.barbearia.barbershop_api.model.Perfil;
import com.barbearia.barbershop_api.model.Servico;
import com.barbearia.barbershop_api.model.Usuario;
import com.barbearia.barbershop_api.repository.AgendamentoRepository;
import com.barbearia.barbershop_api.repository.ServicoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Service
public class ServicoService {

    @Autowired
    private ServicoRepository repository;
    @Autowired
    private AgendamentoRepository agendamentoRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public Servico cadastro(Servico servico) {
        if (servico.getDuracaoMinutos() == null || servico.getDuracaoMinutos() < 0) {
            throw new IllegalArgumentException("Tempo de duração inválido.");
        }
        if (servico.getPreco() == null || servico.getPreco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço inválido.");
        }
        return repository.save(servico);
        //metodo para comparar se o valor é menor que 0 com o compareTo e caso não caia dentro do IF ele salva.
    }

    public List<Servico> listarTudo() {
        return repository.findAll();
    }//metodo que lista todos os servicos da barbearia

    public Servico buscarPorId(Integer id) {
        return repository.findById(id).orElse(null);
    }//faz uma busca de um servico X por id

    public Servico atualizar(Integer id, Servico dadosNovos) {
        Servico servicoAntigo = repository.findById(id).orElse(null);
        if (servicoAntigo == null) {
            return null;
        }
        servicoAntigo.setNome(dadosNovos.getNome());
        servicoAntigo.setDescricao(dadosNovos.getDescricao());
        servicoAntigo.setPreco(dadosNovos.getPreco());
        servicoAntigo.setDuracaoMinutos(dadosNovos.getDuracaoMinutos());
        return repository.save(servicoAntigo);
    }//aqui ele atualizar o servico X buscando ele por Id e salvando os dados novos

    public void excluirId(Integer id) {
        if (agendamentoRepository.existeAgendamentoFuturoParaOServico(id)) {
            throw new IllegalArgumentException("Não é possível excluir o serviço. Existem agendamentos futuros pendentes para ele.");
        }
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Serviço não encontrado;");
        }
        repository.deleteById(id);
    }

    public List<Servico> buscarPorNome(String nome) {
        return repository.findByNomeContainingIgnoreCase(nome);
    }
    @Transactional
    public List<HIstoricoServicoDTO> buscarHistorico(Long id, Usuario usuarioLogado){
        if (usuarioLogado.getPerfil() != Perfil.ADMIN) {
            throw new IllegalArgumentException("Você não tem permissão para realizar esta ação!");
        }
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        List<Object[]> historicoParaTransformar = auditReader.createQuery()
                .forRevisionsOfEntity(Servico.class,false,true)
                .add(AuditEntity.id().eq(id))
                .getResultList();
        return historicoParaTransformar.stream().map(array -> {
            Servico servicoAnterior = (Servico) array[0];
            LogAlteracaoServico logRevisao = (LogAlteracaoServico) array[1];
            RevisionType revisionType = (RevisionType) array[2];
            LocalDateTime dataMudanca = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(logRevisao.getTimestamp()),
                    ZoneId.systemDefault()
            );
            return new HIstoricoServicoDTO(
                    logRevisao.getAutor(),
                    dataMudanca,
                    revisionType.name(),
                    servicoAnterior.getNome(),
                    servicoAnterior.getPreco().doubleValue(),
                    servicoAnterior.getAtivo()
            );
        }).toList();
    }

}
