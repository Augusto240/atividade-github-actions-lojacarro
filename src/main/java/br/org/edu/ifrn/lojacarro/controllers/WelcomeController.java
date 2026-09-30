package br.org.edu.ifrn.lojacarro.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WelcomeController {

    private static final Logger log = LoggerFactory.getLogger(WelcomeController.class);

    @GetMapping("/boas-vindas")
    public ResponseEntity<Map<String, String>> boasVindas() {
        log.debug("Alguem bateu na rota de boas-vindas");
        return ResponseEntity.ok(Map.of("mensagem", "Bem-vindo a LojaCarro API!"));
    }
}
