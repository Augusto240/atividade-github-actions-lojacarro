package br.org.edu.ifrn.lojacarro.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "auditoria")
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, updatable = false, length = 40)
    private String acao;

    @Column(nullable = false, updatable = false, length = 150)
    private String responsavel;

    @Column(nullable = false, updatable = false, length = 1000)
    private String detalhes;

    @Column(nullable = false, updatable = false, length = 64)
    private String hashAnterior;

    @Column(nullable = false, updatable = false, length = 64, unique = true)
    private String hash;

    protected RegistroAuditoria() {
    }

    public RegistroAuditoria(LocalDateTime dataHora, String acao, String responsavel, String detalhes,
            String hashAnterior, String hash) {
        this.dataHora = dataHora;
        this.acao = acao;
        this.responsavel = responsavel;
        this.detalhes = detalhes;
        this.hashAnterior = hashAnterior;
        this.hash = hash;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getAcao() {
        return acao;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public String getHashAnterior() {
        return hashAnterior;
    }

    public String getHash() {
        return hash;
    }
}
