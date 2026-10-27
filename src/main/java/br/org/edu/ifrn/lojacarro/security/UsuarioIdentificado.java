package br.org.edu.ifrn.lojacarro.security;

public record UsuarioIdentificado(Long id, String nome) {

    public String descricao() {
        return nome + " (#" + id + ")";
    }
}
