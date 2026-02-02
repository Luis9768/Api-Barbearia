package com.barbearia.barbershop_api.service;

import com.barbearia.barbershop_api.dto.DadosEntradaDiaEspecial;
import com.barbearia.barbershop_api.model.DiaEspecial;
import com.barbearia.barbershop_api.repository.DiaEspecialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiaEspecialService {

    @Autowired
    private DiaEspecialRepository repository;

    public DiaEspecial cadastro(DadosEntradaDiaEspecial dados){
      if(repository.findByData(dados.data()).isPresent()){
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
}
