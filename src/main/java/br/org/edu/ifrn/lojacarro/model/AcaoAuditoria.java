package br.org.edu.ifrn.lojacarro.model;

public enum AcaoAuditoria {
    CARRO_CRIADO("CARRO"),
    CARRO_ATUALIZADO("CARRO"),
    CARRO_EXCLUIDO("CARRO"),
    VENDA_CRIADA("VENDAS"),
    VENDA_ATUALIZADA("VENDAS"),
    VENDA_EXCLUIDA("VENDAS"),
    TEST_DRIVE_CRIADO("TEST_DRIVE"),
    TEST_DRIVE_ATUALIZADO("TEST_DRIVE"),
    TEST_DRIVE_EXCLUIDO("TEST_DRIVE"),
    LANCAMENTO_CRIADO("CONTABILIDADE"),
    LANCAMENTO_ATUALIZADO("CONTABILIDADE"),
    LANCAMENTO_EXCLUIDO("CONTABILIDADE"),
    ADMINISTRADOR_CRIADO("ADM"),
    ADMINISTRADOR_ATUALIZADO("ADM"),
    ADMINISTRADOR_EXCLUIDO("ADM"),
    LOGS_CONSULTADOS("ADM"),
    ACESSO_NEGADO("SISTEMA");

    private final String modulo;

    AcaoAuditoria(String modulo) {
        this.modulo = modulo;
    }

    public String getModulo() {
        return modulo;
    }
}
