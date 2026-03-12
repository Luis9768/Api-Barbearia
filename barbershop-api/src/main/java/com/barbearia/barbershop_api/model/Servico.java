package com.barbearia.barbershop_api.model;

import com.barbearia.barbershop_api.dto.ServicoDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "tb_servico")
@SQLRestriction("ativo = true")
@SQLDelete(sql = "UPDATE tb_servico SET ativo = false WHERE id = ?")
@Audited
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; //aqui vai definir o ID da tabela como unico e com autoincremento

    @Column(nullable = false, length = 100)
    private String nome; //coluna nome com 100 caracteres no maximo

    private String descricao;

    @Column(nullable = false)
    private BigDecimal preco; // coluna de precos

    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos; //coluna de duração de minutos do serviço

    private Boolean ativo;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] dadosImagem;

    private String tipoImagem;


    //nullable = não pode ser vazia.
    //lenght = tamanho.
    //@Column = coluna

    public Servico() {
    }

    public Servico(String nome, String descricao, Integer duracaoMinutos, BigDecimal preco) {
        this.nome = nome;
        this.descricao = descricao;
        this.duracaoMinutos = duracaoMinutos;
        this.preco = preco;
    }

    public Servico(ServicoDTO dados) {
        this.ativo = true;
        this.nome = dados.getNome();
        this.descricao = dados.getDescricao();
        this.preco = dados.getPreco();
        this.duracaoMinutos = dados.getDuracaoMinutos();
    }
}
