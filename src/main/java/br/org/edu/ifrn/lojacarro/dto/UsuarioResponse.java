package br.org.edu.ifrn.lojacarro.dto;

import br.org.edu.ifrn.lojacarro.model.Usuario;

public class UsuarioResponse {
    private final Long id;
    private final String nome;
    private final String cargo;

    public UsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.cargo = usuario.getCargo().name();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCargo() {
        return cargo;
    }
}
