package br.org.edu.ifrn.lojacarro.model;

public enum UserRole {
    VENDEDOR("VENDEDOR"),
    GERENTE("GERENTE");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
