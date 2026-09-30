package br.org.edu.ifrn.lojacarro.controllers;

import br.org.edu.ifrn.lojacarro.dto.LogFrontendRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
public class LogFrontendController {

    private static final Logger frontend = LoggerFactory.getLogger("FRONTEND");
    private static final int LIMITE_MENSAGEM = 500;

    @PostMapping("/logs")
    public ResponseEntity<Void> receber(@RequestBody LogFrontendRequest request) {
        String mensagem = cortar(request.getMensagem());
        String pagina = cortar(request.getPagina());
        String nivel = request.getNivel() == null ? "INFO" : request.getNivel().toUpperCase(Locale.ROOT);

        switch (nivel) {
            case "ERROR" -> frontend.error("[{}] {}", pagina, mensagem);
            case "WARN" -> frontend.warn("[{}] {}", pagina, mensagem);
            default -> frontend.info("[{}] {}", pagina, mensagem);
        }
        return ResponseEntity.noContent().build();
    }

    private String cortar(String texto) {
        if (texto == null) {
            return "-";
        }
        return texto.length() > LIMITE_MENSAGEM ? texto.substring(0, LIMITE_MENSAGEM) + "..." : texto;
    }
}
