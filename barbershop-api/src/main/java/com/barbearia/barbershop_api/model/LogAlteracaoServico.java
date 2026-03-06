package com.barbearia.barbershop_api.model;

import com.barbearia.barbershop_api.infra.auditoria.AuditoriaListener;
import jakarta.persistence.*;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@Table(name = "tabela_revisao") // O nome bonito que vai aparecer no seu banco
@RevisionEntity(AuditoriaListener.class)
public class LogAlteracaoServico {

    // Avisa o Hibernate quem vai preencher os dados

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber // Diz pro Envers: "Esse é o ID da versão"
    private int id;

    @RevisionTimestamp // Diz pro Envers: "Guarde a data e hora aqui"
    private long timestamp;

    // A NOSSA MÁGICA ENTRA AQUI! 🪄
    @Column(name = "autor_alteracao")
    private String autor;

    // Não esqueça de gerar os Getters e Setters (ou usar @Data do Lombok)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }
}

