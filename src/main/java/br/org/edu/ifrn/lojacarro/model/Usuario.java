package br.org.edu.ifrn.lojacarro.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Cargo cargo;

    public Usuario() {
    }

    public Usuario(String nome, Cargo cargo) {
        this.nome = nome;
        this.cargo = cargo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public String identificacao() {
        return nome + " #" + id + " (" + cargo + ")";
    }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", nome='" + nome + "', cargo=" + cargo + "}";
    }
}
