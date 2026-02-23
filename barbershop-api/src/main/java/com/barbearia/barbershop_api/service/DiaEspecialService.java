package com.barbearia.barbershop_api.service;

import com.barbearia.barbershop_api.dto.DadosEntradaDiaEspecial;
import com.barbearia.barbershop_api.model.DiaEspecial;
import com.barbearia.barbershop_api.model.Perfil;
import com.barbearia.barbershop_api.model.Usuario;
import com.barbearia.barbershop_api.repository.DiaEspecialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiaEspecialService {

    @Autowired
    private DiaEspecialRepository repository;

    public DiaEspecial cadastro(DadosEntradaDiaEspecial dados, Usuario usuarioLogado) {
        if (usuarioLogado.getPerfil() != Perfil.ADMIN) {
            throw new IllegalArgumentException("Você não tem permissão para realizar esta ação!");
        }
            if (repository.findByData(dados.data()).isPresent()) {
                throw new IllegalArgumentException("Já existe uma configuração para esta " +
                        "data. Use a edição se quiser alterar.");
            }
            DiaEspecial diaEspecial = new DiaEspecial();
            diaEspecial.setData(dados.data());
            diaEspecial.setDiaFolga(dados.diaFolga());
            diaEspecial.setDescricao(dados.descricao());
            diaEspecial.setHorarioAbertura(dados.horaAbertura());
            diaEspecial.setHorarioFechamento(dados.horaFechamento());

            return repository.save(diaEspecial);

        }
        public List<DiaEspecial> listarDiaEspecial () {
            return repository.findAll();
        }

    }
