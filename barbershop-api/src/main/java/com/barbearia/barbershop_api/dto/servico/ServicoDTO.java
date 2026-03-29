package com.barbearia.barbershop_api.dto.servico;

import com.barbearia.barbershop_api.entity.Servico;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@JsonPropertyOrder({"nome", "preco", "descricao", "duracaoMinutos"})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServicoDTO {

    private Integer id;

    @NotBlank(message = "O nome é obrigatório")
    private String nome; //coluna nome com 100 caracteres no maximo

    private String descricao;

    @Positive(message = "Preço não pode ser negativo")
    private BigDecimal preco; // coluna de precos

    @Positive(message = "Tempo não pode ser negativo")
    private Integer duracaoMinutos;


    public ServicoDTO(Servico servico) {
        this.id = servico.getId();
        this.preco = servico.getPreco();
        this.nome = servico.getNome();
        this.descricao = servico.getDescricao();
        this.duracaoMinutos = servico.getDuracaoMinutos();
    }
}
