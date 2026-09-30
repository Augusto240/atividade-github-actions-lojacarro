package br.org.edu.ifrn.lojacarro.dto;

public class UsuarioRequest {
    private String nome;
    private String cargo;

    public UsuarioRequest() {
    }

    public UsuarioRequest(String nome, String cargo) {
        this.nome = nome;
        this.cargo = cargo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }
}
