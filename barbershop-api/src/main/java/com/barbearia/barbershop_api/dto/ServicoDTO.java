package com.barbearia.barbershop_api.dto;

import com.barbearia.barbershop_api.model.Servico;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class ServicoDTO {

    @NotBlank(message = "O nome é obrigatório")
    private String nome; //coluna nome com 100 caracteres no maximo

    private String descricao;

    @Positive(message = "Preço não pode ser negativo")
    private BigDecimal preco; // coluna de precos

    @Positive(message = "Tempo não pode ser negativo")
    private Integer duracaoMinutos; //coluna de duração de minutos do serviço

    //nullable = não pode ser vazia.

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }
    //lenght = tamanho.
    //@Column = coluna


    public ServicoDTO(Servico servico) {
        this.preco = servico.getPreco();
        this.nome = servico.getNome();
        this.descricao = servico.getDescricao();
        this.duracaoMinutos = servico.getDuracaoMinutos();
    }
    public ServicoDTO(){}
}
