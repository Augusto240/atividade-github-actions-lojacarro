package br.org.edu.ifrn.lojacarro.dto;

public class IntegridadeAuditoriaResponse {

    private final boolean integra;
    private final int totalRegistros;
    private final Long registroAdulterado;
    private final String mensagem;

    private IntegridadeAuditoriaResponse(boolean integra, int totalRegistros, Long registroAdulterado, String mensagem) {
        this.integra = integra;
        this.totalRegistros = totalRegistros;
        this.registroAdulterado = registroAdulterado;
        this.mensagem = mensagem;
    }

    public static IntegridadeAuditoriaResponse integra(int totalRegistros) {
        return new IntegridadeAuditoriaResponse(true, totalRegistros, null, "Nenhuma alteração encontrada");
    }

    public static IntegridadeAuditoriaResponse adulterada(int totalRegistros, Long registroAdulterado) {
        return new IntegridadeAuditoriaResponse(false, totalRegistros, registroAdulterado,
                "Registro " + registroAdulterado + " foi alterado ou apagado");
    }

    public boolean isIntegra() {
        return integra;
    }

    public int getTotalRegistros() {
        return totalRegistros;
    }

    public Long getRegistroAdulterado() {
        return registroAdulterado;
    }

    public String getMensagem() {
        return mensagem;
    }
}
