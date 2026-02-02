package com.barbearia.barbershop_api.service;

import com.barbearia.barbershop_api.model.Servico;
import com.barbearia.barbershop_api.repository.AgendamentoRepository;
import com.barbearia.barbershop_api.repository.ServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ServicoService {

    @Autowired
    private ServicoRepository repository;
    @Autowired
    private AgendamentoRepository agendamentoRepository;

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

    public void excluirId(Integer id){
        if(agendamentoRepository.existeAgendamentoFuturoParaOServico(id)){
            throw new IllegalArgumentException("Não é possível excluir o serviço. Existem agendamentos futuros pendentes para ele.");
        }
        if (!repository.existsById(id)){
            throw new IllegalArgumentException("Serviço não encontrado;");
        }
        repository.deleteById(id);
    }

}
