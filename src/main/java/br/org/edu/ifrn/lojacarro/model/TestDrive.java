package br.org.edu.ifrn.lojacarro.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_drive")
public class TestDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long carroId;

    @Column(nullable = false, length = 100)
    private String cliente;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(length = 255)
    private String observacao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCarroId() {
        return carroId;
    }

    public void setCarroId(Long carroId) {
        this.carroId = carroId;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    @Override
    public String toString() {
        return "test drive #" + id + " do carro #" + carroId + " com " + cliente + " em " + dataHora;
    }
}
