package com.barbearia.barbershop_api.service;

import com.barbearia.barbershop_api.dto.DadosEntradaDiaEspecial;
import com.barbearia.barbershop_api.dto.SaidaDiaEspecialDTO;
import com.barbearia.barbershop_api.model.DiaEspecial;
import com.barbearia.barbershop_api.model.Perfil;
import com.barbearia.barbershop_api.model.Usuario;
import com.barbearia.barbershop_api.repository.DiaEspecialRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiaEspecialService {

    @Autowired
    private DiaEspecialRepository repository;

    @Transactional
    public SaidaDiaEspecialDTO cadastro(DadosEntradaDiaEspecial dados, Usuario usuarioLogado) {
        if (usuarioLogado.getPerfil() != Perfil.ADMIN) {
            throw new IllegalArgumentException("Você não tem permissão para realizar esta ação!");
        }
        validarDataPassado(dados.data());

        if (repository.findByData(dados.data()).isPresent()) {
            throw new IllegalArgumentException("Já existe uma configuração para esta data. Use a edição se quiser alterar.");
        }
        DiaEspecial diaEspecial = new DiaEspecial();
        diaEspecial.setData(dados.data());
        diaEspecial.setDiaFolga(dados.diaFolga());
        diaEspecial.setDescricao(dados.descricao());
        if (diaEspecial.getDiaFolga() == true) {
            diaEspecial.setHorarioAbertura(null);
            diaEspecial.setHorarioFechamento(null);
        } else {
            diaEspecial.setHorarioFechamento(dados.horaFechamento());
            diaEspecial.setHorarioAbertura(dados.horaAbertura());
        }
        repository.save(diaEspecial);
        return new SaidaDiaEspecialDTO(diaEspecial);

    }

    public List<DiaEspecial> listarDiaEspecial() {
        return repository.findAll();
    }

    private void validarDataPassado(LocalDate data) {
        if (data.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Não é permitido agendar no passado! Selecione uma data futura.");
        }
    }

    @Transactional
    public SaidaDiaEspecialDTO atualizarDiaEspecial(int id, DadosEntradaDiaEspecial dados, Usuario usuarioLogado) {
        if (usuarioLogado.getPerfil() != Perfil.ADMIN) {
            throw new IllegalArgumentException("Você não tem permissão para realizar esta ação!");
        }
        DiaEspecial diaEspecial = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Dia Especial não encontrado!"));
        validarDataPassado(diaEspecial.getData());

        diaEspecial.setData(dados.data());
        diaEspecial.setDiaFolga(dados.diaFolga());
        diaEspecial.setDescricao(dados.descricao());
        if (diaEspecial.getDiaFolga() == true) {
            diaEspecial.setHorarioAbertura(null);
            diaEspecial.setHorarioFechamento(null);
        } else {
            diaEspecial.setHorarioFechamento(dados.horaFechamento());
            diaEspecial.setHorarioAbertura(dados.horaAbertura());
        }
        repository.save(diaEspecial);
        return new SaidaDiaEspecialDTO(diaEspecial);
    }

    public void deletarDiaEspecial(int id, Usuario usuarioLogado) {
        if (usuarioLogado.getPerfil() != Perfil.ADMIN) {
            throw new IllegalArgumentException("Você não tem permissão para realizar esta ação!");
        }
        var buscarDia = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Dia Especial não encontrado!"));
        repository.delete(buscarDia);
    }

    public DiaEspecial buscarPorId(int id) {
        var buscarDia = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Dia Especial não encontrado!"));
        return buscarDia;
    }
}
